import akka.actor.Actor
import main.system

// This company bids for Plumbing only
// Strategy: fixed price

class CompanyC() extends Actor {

  //La company effettua il subscribe agli eventi di tipo auctionStatus
  system.eventStream.subscribe(self, classOf[auctionStatus])

  var winningAuction = new Array[Boolean](8)
  for (b<-0 to winningAuction.size-1){
    winningAuction(b)=false
  }

  //Different Fixed price for each work (except task 0 that is not runnable for this company)
  val myPrice = Map(1 -> 900, 2 -> 900, 3 -> 1100, 4 -> 2000, 5 -> 600, 6 -> 300, 7 -> 1100)


  def receive = {
    //Questo tipo di messaggio arriva alla creazione dell'asta o ogni qual volta cambia il currentwinner
    case m: auctionStatus       ⇒

      if (m.currentWinner==self){
        winningAuction(m.task)=true
      }else{
        winningAuction(m.task)=false
      }

      //La companyC può vincere al massimo due aste
      //if (m.currentWinner==null){

        for((t,p) <- myPrice){

          var count=0
          for (c<-0 to winningAuction.size-1){
            if(winningAuction(c)==true){
              count +=1
            }
          }
          //Verifico che l'asta riguardi un lavoro che è tra le mie skills e inoltre che non stia vincendo più di due aste
          if (m.task==t && count<3){

            //Se il lavoro è tra le mie skills verifico di poter fare un'offerta migliore di quella
            if(m.currentBid>p && m.currentWinner != self){
              var myBid = new bidMessage(p)
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