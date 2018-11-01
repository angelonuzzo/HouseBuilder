
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

  var casagiacomo = new casa("0")

  val BuilderA = system.actorOf(Props(classOf[BuilderA]),"BuilderA")
  val BuilderB = system.actorOf(Props(classOf[BuilderB]),"BuilderB")
  val BuilderC = system.actorOf(Props(classOf[BuilderC]),"BuilderC")
  val BuilderD = system.actorOf(Props(classOf[BuilderD]),"BuilderD")
  val BuilderE = system.actorOf(Props(classOf[BuilderE]),"BuilderE")
  val BuilderF = system.actorOf(Props(classOf[BuilderF]),"BuilderF")
  val BuilderG = system.actorOf(Props(classOf[BuilderG]),"BuilderG")

  val orchestrator = system.actorOf(Props(classOf[Orchestrator]),"OrchestratorA")

  val startMessage = new workStartMessage(casagiacomo)

  orchestrator ! startMessage

  class BuilderA extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+A")
        reply.status+="+A"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderB extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+B")
        reply.status+="+B"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderC extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+C")
        reply.status+="+C"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderD extends Actor {
    def receive = {
      case house: casa ⇒
        Thread.sleep(1000)
        val reply = house//.status.concat("+D")
        reply.status+="+D"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderE extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+E")
        reply.status+="+E"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderF extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+F")
        reply.status+="+F"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
        sender() ! reply // reply to the ask
    }
  }

  class BuilderG extends Actor {
    def receive = {
      case house: casa ⇒
        val reply = house//.status.concat("+G")
        reply.status+="+G"
        println(s"Sono ${self.path.name} e ho completato il mio lavoro. Lo stato attuale della casa è ${casagiacomo.status}")
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

      case m:casa =>
        casagiacomo.status="Completata!"
        println(s"Sono ${self.path.name} e lo stato attuale della casa è ${casagiacomo.status}")
        system.terminate()

      case m:workStartMessage =>
        
        println(s"Sono ${self.path.name} e sto per fare costruire la casa ${m.house}, lo stato attuale della casa è ${m.house.status}")



        val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
          val S: Outlet[casa]             = builder.add(Source.single(m.house)).out
          val A: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderA))
          val B: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderB))
          val C: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderC))
          val b1 = builder.add(Broadcast[casa](3))
          val D: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderD))
          val E: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderE))
          val F: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderF))
          val zip = builder.add(ZipWith[casa, casa, casa, casa](zipper = (A1:casa,A2:casa, A3:casa)=>m.house))
          val G: FlowShape[casa, casa]     = builder.add(Flow[casa].ask[casa](parallelism = 5)(BuilderG))

          val Z: Inlet[Any]              = builder.add(Sink.foreach[Any](_ =>    self ! m.house)).in


          S ~> A  ~>  B ~> C  ~>  b1 ~>  D ~> zip.in0
                                  b1 ~>  E ~> zip.in1
                                  b1 ~> F ~> zip.in2
                                              zip.out ~> G ~> Z
          stream.ClosedShape
        })

        println(s"Sono ${self.path.name} e lo stato attuale della casa è ${casagiacomo.status}")

        graph.run()

    }
  }

}
