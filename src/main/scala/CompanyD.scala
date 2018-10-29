import akka.actor.Actor
import main.system

// This company bids for Plumbing only
// Strategy: fixed price

class CompanyD() extends Actor {

  //La company effettua il subscribe agli eventi di tipo auctionStatus
  system.eventStream.subscribe(self, classOf[auctionStatus])


  def receive = {
    //Questo tipo di messaggio arriva alla creazione dell'asta o ogni qual volta cambia il currentwinner
    case m: auctionStatus       ⇒


      //La companyC punta solo all'inizio dell'asta
      if (m.currentWinner==null){

        val r = scala.util.Random
        val myPrice = r.nextInt(1000)
        var myBid = new bidMessage(myPrice+800)
        m.auctioRef ! myBid

      }

    case m:winnerCongrats =>





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