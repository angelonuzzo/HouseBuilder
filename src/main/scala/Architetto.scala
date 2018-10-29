import akka.actor.{Actor, ActorRef, _}
import akka.util.Timeout
import akka.pattern.ask

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class Architetto(var vincitori:Array[ActorRef], var lavori:Array[String], var precedenze:Array[String]) extends Actor {


  var vincitoriMap = scala.collection.mutable.Map[String,ActorRef]()
  var completati=ArrayBuffer(-1)
  var pending=new ArrayBuffer[Int]
  implicit val timeout = Timeout(5 seconds)

  var numCompletati:Int=0
 // var triggers=Array[Int](3)
  //for (x <- 0 to triggers.size-1){
    //triggers(x)=lavori.size+1
  //}

  var x:Int=0
  for( x <- 0 to lavori.size-1 ){
    vincitoriMap += (lavori(x) -> this.vincitori(x))
  }

  var precedenzeMap = scala.collection.mutable.Map[String,String]()
  for (x <- 0 to lavori.size-1){
    precedenzeMap += (lavori(x) -> this.precedenze(x))
  }

  x=0
  for( x <- 0 to lavori.size-1 ){
    vincitoriMap += (lavori(x) -> this.vincitori(x))
  }



  override def receive = {
    case "ciao" =>


      println(s"ciao a te ${sender.path.name} e ti dico anche che i vincitori sono:")
      for ((k, v) <- vincitoriMap) {
        println(s"${k} ----------------->  ${v.path.name}")
      }
      self ! "start"

    case "start" =>
      println(s"Ok, iniziamo con i lavori")
      var firstWork = new StartWork(0, lavori)
      vincitoriMap(lavori(0)) ! firstWork

    case m: WorkCompleted =>

      println(s"Ok, il lavoro di ${lavori(m.work)} è stato completato")
      this.completati+= m.work
      numCompletati += 1
      var q:Int= -1
      for (l<- 0 to pending.size-1){
        if(m.work==pending(l)){
          q=l
        }
      }
      if(q != -1){
        pending(q)= -1
      }

      //var precedenzeBuffer = ArrayBuffer
      //var checkBuffer = ArrayBuffer
      //var completedBuffer = ArrayBuffer
      var check:Boolean=false
      var available = new ArrayBuffer[Int]
      
      
      
      // i= lavoro
      // s= nullcheck
      // t= toInt conversion
      // k= precedenza
      // t= loop completati
      // w= loop checklist
      // r= available
      


      // Per ogni lavoro
      for (i <- 0 to lavori.size - 1) {
        
        // Verifico se il lavoro in questio è già stato realizzato
        var nullcheck = false
        
        // Scorro l'elenco dei completati
        for (s <- 0 to numCompletati) {
          
          // Se il lavoro i è tra i completati, rendo nullcheck vero
          if (i == this.completati(s))  {
            nullcheck = true
          }
        }
        for (b<-0 to pending.size-1){
          if (i==this.pending(b)){
            nullcheck=true
          }
        }
        
        // Eseguo il resto solo se nullcheck è rimasto true
        if (nullcheck == false) {

          // Costruisco l'array delle precedenze per il lavoro i
          var precedenzeArray = (precedenzeMap(lavori(i))).split(",")

          // Lo trasformo in interi
          var precedenzeArrayInt = new Array[Int](precedenzeArray.size)
          for (t <- 0 to precedenzeArray.size - 1) {
            precedenzeArrayInt(t)=precedenzeArray(t).toInt
          }

          // Definisco l'array checklist

          var checkList = new Array[Boolean](precedenzeArrayInt.size)
          for (n<-0 to checkList.size-1){
            checkList(n)=false
          }


          // Per ogni precedenza verifico che il relativo lavoro sia stato completato
          for (k <- 0 to precedenzeArrayInt.size - 1) {

            //Condizioni iniziali
            var t: Int = 0
            check = false

            // Check resta false finchè la precedenza k non è soddisfatta da uno dei lavori completati
            while (check == false && t < this.completati.size) {
              if (precedenzeArrayInt(k) == this.completati(t)) {
                check = true
              } else {
                t += 1
              }
            }
            if (check == true) {
              checkList(k) = true
            }
          }
          var totalCheck = true
          for (w <- 0 to checkList.size - 1) {
            if (checkList(w) == false) {
              totalCheck = false
            }
          }
          if (totalCheck == true) {
            available += i
          }

        }

      }
      if (available.size>0){
      println("++++++++++++++++++++++++++++++++++++++++++")
      println(s"Possono essere avviati i seguenti lavori:")
      for(r<-0 to available.size-1){
        var currentWork = new StartWork(available(r), lavori)
        println(lavori(available(r)))


        val future = vincitoriMap(lavori(available(r))) ? currentWork
        val result = Await.result(future, timeout.duration).asInstanceOf[AnyVal]
        self ! result

        //vincitoriMap(lavori(available(r))) ! currentWork
        pending += available(r)

      }
      println("++++++++++++++++++++++++++++++++++++++++++")
      }
      if (numCompletati==10){
        println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        println("--------LA CASA E' PRONTA!------------")
        println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        context.system.terminate()
      }

  }
}