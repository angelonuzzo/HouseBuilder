import akka.actor.Actor
import main.system

class AuctionManager(var taskID: Int, var taskName: String, var maxValue: Int) extends Actor{

  var currentWinner=self
  var currentBid=maxValue

  // Comunico la creazione dell'asta attraverso l'event bus
  val newAuction = new auctionStatus(taskID,maxValue,null, self)
  system.eventStream.publish(newAuction)


  // Il metodo receive è diviso in due stati (astaAperta o chiusa)
  def receive = astaAperta

  val astaAperta : Receive = {

    // Quando ricevi un'offerta...
    case m:bidMessage=>

      // Se l'offerta è più bassa della currentBid
      if(m.value<currentBid && currentWinner!= sender()){

        //Aggiorno l'attuale vincitore e il valore della relativa offerta
        currentBid=m.value
        currentWinner=sender()

        println(s"Nuova offerta migliore per l'asta ${taskName}: ${sender().path.name} ha offerto ${m.value}")

        //Faccio uno stream comunicando il nuovo stato dell'asta
        val newStatus = new auctionStatus(taskID,currentBid,currentWinner,self)
        system.eventStream.publish(newStatus)
      }

    // Questa è la risposta alla future in cui l'auctioneer mi chiede il vincitore
    case m:showWinners=>

      context.become(astaChiusa)

      val endStatus = new auctionStatus(taskID,currentBid,currentWinner,self)
      sender() ! endStatus

  }

  // Se l'asta è stata chiusa
  val astaChiusa : Receive = {

    // Quando mi arriva un'offerta non posso accettarla
    case m:bidMessage=>
      sender() ! "I'm sorry, i'm dying!"


  }



}
