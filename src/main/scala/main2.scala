
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.util.Timeout

import scala.concurrent.duration._






object main2 extends App {

  implicit val askTimeout = Timeout(5 seconds)
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  var casagiacomo = new casa("ciao")

  val BuilderA = system.actorOf(Props(classOf[BuilderA]),"BuilderA")
  val BuilderB = system.actorOf(Props(classOf[BuilderB]),"BuilderB")
  val BuilderC = system.actorOf(Props(classOf[BuilderC]),"BuilderC")
  val BuilderD = system.actorOf(Props(classOf[BuilderD]),"BuilderD")
  val BuilderE = system.actorOf(Props(classOf[BuilderE]),"BuilderE")
  val BuilderF = system.actorOf(Props(classOf[BuilderF]),"BuilderF")
  val BuilderG = system.actorOf(Props(classOf[BuilderG]),"BuilderG")

  val orchestrator = system.actorOf(Props(classOf[Orchestrator]),"OrchestratorA")

  orchestrator ! "inizia"

  class BuilderA extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+A")
        reply.boh="ciao+A"
        sender() ! reply // reply to the ask
    }
  }

  class BuilderB extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+B")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderC extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+C")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderD extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+D")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderE extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+E")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderF extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+F")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderG extends Actor {
    def receive = {
      case house: casa ⇒
        println(s"Sono ${self.path.name} e ho completato il mio lavoro")
        val reply = house//.boh.concat("+G")
        sender() ! reply // reply to the ask
    }
  }

  class Absoluter extends Actor {
    def receive = {
      case num: Int ⇒
        val reply = num.abs
        sender() ! reply // reply to the ask
    }
  }

  class Orchestrator extends Actor {
    override def receive: Receive = {
      case "inizia" =>

        val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
          val S: Outlet[casa]             = builder.add(Source.single(casagiacomo)).out
          val A: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderA))
          val B: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderB))
          val C: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderC))
          val b1 = builder.add(Broadcast[casa](3))
          val D: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderD))
          val E: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderE))
          val F: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderF))
          val zip = builder.add(ZipWith[casa, casa, casa, casa](zipper = (A1:casa,A2:casa, A3:casa)=>casagiacomo))
          val G: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderG))

          val Z: Inlet[Any]              = builder.add(Sink.foreach(println)).in


          S ~> A  ~>  B ~> C  ~>  b1 ~>  D ~> zip.in0
                                  b1 ~>  E ~> zip.in1
                                  b1 ~> F ~> zip.in2
                                              zip.out ~> G ~> Z
          stream.ClosedShape
        })


        graph.run()

    }
  }

}
