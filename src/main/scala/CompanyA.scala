import akka.actor.Actor
import main.{system}

// This company bids for Plumbing only
// Strategy: fixed price

class CompanyA() extends Actor {

  //La company effettua il subscribe agli eventi di tipo auctionStatus
  system.eventStream.subscribe(self, classOf[auctionStatus])

  //Fixed price
  val myPrice:Int=300

  //Questo array rappresenta i lavori che la company è in grado di eseguire
  val skills=Array[Int](5)




  def receive = {
    //Questo tipo di messaggio arriva alla creazione dell'asta o ogni qual volta cambia il currentwinner
    case m: auctionStatus       ⇒
      //Verifico che l'asta riguardi un lavoro che è tra le mie skills
      for (i <- 0 to skills.size-1) {

        if (m.task==this.skills(i)){

          //Se il lavoro è tra le mie skills verifico di poter fare un'offerta migliore di quella
          if(m.currentBid>myPrice && m.currentWinner != self){
            var myBid = new bidMessage(myPrice)
            //Offro myPrice
            m.auctioRef ! myBid
          }
        }
      }

    case m:StartWork =>
      println(s"Sono ${self.path.name} e sto eseguendo il lavoro ${m.lavori(m.work)}")
      Thread.sleep(2000)
      println(s"Sono ${self.path.name} e ho eseguito il lavoro ${m.lavori(m.work)}")
      val workCompl = new WorkCompleted(m.work)
      sender() ! workCompl

    case "I'm sorry, i'm dying!" =>
      println("Just few milliseconds...")
  }


}