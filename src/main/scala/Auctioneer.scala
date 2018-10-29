import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await

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
      system.scheduler.scheduleOnce(5000 milliseconds /*1*/, self, stopAuction)

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



    //var byeBye = new killMessage(sender())
    //system.scheduler.scheduleOnce(5000 milliseconds, self, byeBye)

    //Termino l'attore relativo all'asta
    Thread.sleep(1000)
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
      self ! "startWorks"
    }

  }

  // Il metodo receive richiama i due metodi
  def receive = {
    case "start" =>

      buildAuctions()


    case m:AuctionEndMessage =>

      stopAuction(m)

    case m:killMessage =>
//      context.stop(m.dyingActor)


    case "startWorks" =>
      /*
      var counter:Int=0
      for ((k,v) <- vincitori){
        ArrayVincitori(counter)=v
        counter+=1
      }

      val Architetto = context.actorOf(Props(classOf[Architetto],ArrayVincitori,objects,precedenzeLavori),s"architetto")
      println(s"Diamo il benvenuto all'architetto Vakka che dirigerà i lvori di costruzione")
      Architetto ! "ciao"

*/




  }

}
