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

  var taskNames = new Array[String](10)

  taskNames(0)="SitePreparation"
  taskNames(1)="Floors"
  taskNames(2)="Walls"
  taskNames(3)="Roof"
  taskNames(4)="WindowsDoors"
  taskNames(5)="Plumbing"
  taskNames(6)="ElectricalSystem"
  taskNames(7)="Painting"
  taskNames(8)=""
  taskNames(9)=""

  var myWorks = Array(false,false,false,false,false,false,false,false,false,false,false)





  def receive = {
    //Questo tipo di messaggio arriva alla creazione dell'asta o ogni qual volta cambia il currentwinner
    case m: auctionStatus       ⇒
      //Verifico che l'asta riguardi un lavoro che è tra le mie skills
      if (m.open==true){
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
      }else{
        if(m.currentWinner==self){
          myWorks(m.task)=true
          println(s"Evviva! Sono ${self.path.name} e ho vinto l'asta per ${taskNames(m.task)}")
        }

      }
    case "I'm sorry, i'm dying!" =>
      println("Just few milliseconds...")

    case m: buildingHouse ⇒
      var currentWork=0
      for(i<-0 to myWorks.size-1){
        if(myWorks(i)==true){
          currentWork=i
        }
      }
      currentWork match{
        case 4 =>
          taskNames(4)="Windows"
          taskNames(8)="Doors"
          myWorks(8)=true

        case 7 =>
          taskNames(7)="ExtPaintings"
          taskNames(9)="IntPaintings"
          myWorks(9)=true

        case _ =>

      }
      m.status+="+"+taskNames(currentWork)
      println(s"Sono ${self.path.name} e ho completato il lavoro ${taskNames(currentWork)}. Lo stato attuale della casa è ${m.status}")
      sender() ! m // reply to the ask
      myWorks(currentWork)=false
  }


}