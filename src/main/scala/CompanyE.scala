import akka.actor.Actor
import main.system

// This company bids for Plumbing only
// Strategy: fixed price

class CompanyE() extends Actor {

  //La company effettua il subscribe agli eventi di tipo auctionStatus
  system.eventStream.subscribe(self, classOf[auctionStatus])

  var winningAuction = new Array[Boolean](8)
  for (b<-0 to winningAuction.size-1){
    winningAuction(b)=false
  }

  //Different Fixed price for each work (except task 0 that is not runnable for this company)
  val myPrice = Map(1 -> 800, 2 -> 800, 3 -> 800)


  def receive = {
    //Questo tipo di messaggio arriva alla creazione dell'asta o ogni qual volta cambia il currentwinner
    case m: auctionStatus       ⇒

      //Aggiorno il mio winningAuction in base al contenuto del messaggio
      if (m.currentWinner==self){
        winningAuction(m.task)=true
      }else{
        winningAuction(m.task)=false
      }

        //Scorro la lista di myPrice
        for((t,p) <- myPrice){

          //Verifico che l'asta riguardi un lavoro che è tra le mie skills
          if (m.task==t){

            //Questa variabile esprimerà il valore che ho già offerto per le aste che sto attualmente vincendo
            var sum:Int=0

            //Con questo ciclo calcolo il valore di sum
            for(w<-0 to winningAuction.size-1){
              if (winningAuction(w)==true){
                sum += myPrice(w)
              }
            }
            //Verifico di poter fare un'offerta migliore di quella, date le offerte già fatte sulle aste che sto vincendo
            if(m.currentBid +sum > p && m.currentWinner != self){
              //Offro il valore più alto tra il myPrice e il currentBid-10
              var myBid = new bidMessage(scala.math.max(p,m.currentBid-10))
              //Offro il mio fixed price
              m.auctioRef ! myBid
            }
          }
        }
      //}

    case m:winnerCongrats =>

      winningAuction(m.auctionID)=true



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