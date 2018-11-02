import akka.actor.{Actor, ActorRef, Props, _}
import akka.pattern.ask
import akka.stream
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.stream.{FlowShape, Inlet, Outlet}
import akka.util.Timeout
import main.materializer

import scala.concurrent.Await
import scala.concurrent.duration._

class Auctioneer() extends Actor{
  import context._

  // Array contenente i nomi dei tasks
  val taskNames = new Array[String](8)

  taskNames(0)="SitePreparation"
  taskNames(1)="Floors"
  taskNames(2)="Walls"
  taskNames(3)="Roof"
  taskNames(4)="WindowsDoors"
  taskNames(5)="Plumbing"
  taskNames(6)="ElectricalSystem"
  taskNames(7)="Painting"

  // Array contenente i maxPrices
  val maxPrices=new Array[Int](8)

  maxPrices(0)= 2000
  maxPrices(1)= 1000
  maxPrices(3)= 1000
  maxPrices(2)= 2000
  maxPrices(4)= 2500
  maxPrices(5)= 500
  maxPrices(6)= 500
  maxPrices(7)= 1200

  // Array contenente le precedenze per i lavori
  val taskPrec = new Array[String](8)

  taskPrec(0)= null
  taskPrec(1)= "0"
  taskPrec(2)="1"
  taskPrec(3)="0,1,2"
  taskPrec(4)="0,1,2"
  taskPrec(5)="3,4"
  taskPrec(6)="3,4"
  taskPrec(7)="5,6"

  // Array contenente i vincitori delle aste (verrà riempito dopo)
  var auctionWinners = new Array[ActorRef](8)


  //Questa funzione crea tutte le aste e schedula il messaggio di fine dell'asta stessa
  def buildAuctions () = {

    for(n<-0 to taskNames.size-1){

      println(s"--------------------------   ASTA PER ${taskNames(n).toUpperCase()} CREATA   --------------------------")

      // Qui genero l'attore auctionManager che gestirà l'asta ennesima
      val Auctionx = context.actorOf(Props(classOf[AuctionManager], n, taskNames(n),maxPrices(n)),s"auction-${taskNames(n)}")

      // Qui creo un messaggio di fine asta e lo schedulo per essermi inviato tra "delay"
      val stopAuction = new AuctionEndMessage(Auctionx,n)
      system.scheduler.scheduleOnce(1 milliseconds /*1*/, self, stopAuction)

    }
  }

  // Questa funzione gestisce la terminazione dell'asta (viene triggerato dal message auto-schedulato)
  def stopAuction (m: AuctionEndMessage) = {
    println(s"L'asta per ${taskNames(m.auctionID)} è terminata")

    // Qui uso un future per chiedere all'asta di dirmi chi è il vincitore (e il relativo price)
    val yourWinners = new showWinners
    implicit val timeout = Timeout(1 seconds)
    val askWinners = m.auction ? yourWinners
    val winner = Await.result(askWinners, timeout.duration).asInstanceOf[auctionStatus]

    // Registro il vincitore dell'asta nella mia conoscenza
    auctionWinners(m.auctionID)=winner.currentWinner
    println(s"Il vincitore per l'asta ${taskNames(winner.task)} è ${winner.currentWinner.path.name} con un'offerta di ${winner.currentBid} (max: ${maxPrices(winner.task)})")


    //Termino l'attore relativo all'asta
    context.stop(m.auction)


    // Con questo blocco di codice verifico se le aste sono tutte terminate e in tal caso mi invio "startWorks"
    var auctionCompleted = true

    for (i<-0 to auctionWinners.size-1){
      if(auctionWinners(i) == null){
        auctionCompleted=false
      }
    }

    if (auctionCompleted==true){
      println("Tutte le aste sono state aggiudicate, iniziamo con i lavori!")
      println("Ecco un riepilogo dei vincitori:")
      for(i<-0 to auctionWinners.size-1){
        println(s"${taskNames(i)} ----------->   ${auctionWinners(i).path.name}.")
      }
      val houseToBuild = new buildingHouse("WorksCommitted","Giacomo",0)
      val letsBuildIt = new startWorkMessage(houseToBuild)

      self ! letsBuildIt
    }

  }

  // Il metodo receive richiama i due metodi
  def receive = {
    case "start" =>

      buildAuctions()


    case m:AuctionEndMessage =>

      stopAuction(m)


    case m:startWorkMessage =>

      println(s"Sono ${self.path.name} e sto per fare costruire la casa di ${m.house.owner}, lo stato attuale della casa è ${m.house.status}")



      implicit val askTimeout = Timeout(5 seconds)


      val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
        val S: Outlet[buildingHouse]             = builder.add(Source.single(m.house)).out
        val A: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(0)))
        val B: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(1)))
        val C: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(2)))
        val b1 = builder.add(Broadcast[buildingHouse](3))
        val D: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(3)))
        val E: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(4)))
        val F: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(4)))
        val zip = builder.add(ZipWith[buildingHouse, buildingHouse, buildingHouse, buildingHouse](zipper = (A1:buildingHouse,A2:buildingHouse, A3:buildingHouse)=>m.house))
        val Q: FlowShape[buildingHouse, buildingHouse]          = builder.add(Flow[buildingHouse].map(elem => elem))
        val b2 = builder.add(Broadcast[buildingHouse](3))
        val G: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(5)))
        val H: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(6)))
        val I: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(7)))
        val zip2 = builder.add(ZipWith[buildingHouse, buildingHouse, buildingHouse, buildingHouse](zipper = (B1:buildingHouse,B2:buildingHouse, B3:buildingHouse)=>m.house))
        val J: FlowShape[buildingHouse, buildingHouse]     = builder.add(Flow[buildingHouse].ask[buildingHouse](parallelism = 5)(auctionWinners(7)))
        val Z: Inlet[Any]              = builder.add(Sink.foreach[Any](_ =>    self ! m.house)).in



        S ~> A  ~>  B ~> C  ~>  b1 ~>  D ~> zip.in0
                                b1 ~>  E ~> zip.in1
                                b1 ~> F ~> zip.in2
                                                      zip.out ~>  Q ~>  b2 ~>  G ~> zip2.in0
                                                                        b2 ~>  H ~> zip2.in1
                                                                        b2 ~>  I ~> zip2.in2
                                                                                                zip2.out ~> J ~> Z

        stream.ClosedShape

      })


      graph.run()

    case m:buildingHouse =>
      m.status="Completata!"
      println(s"Sono ${self.path.name} e lo stato attuale della casa è ${m.status}")
      println("Complimenti a noi!!!!!")




  }

}
