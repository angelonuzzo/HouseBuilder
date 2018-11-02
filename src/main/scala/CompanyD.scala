import akka.actor.Actor
import main.system

// This company bids for Plumbing only
// Strategy: fixed price

class CompanyD() extends Actor {

  //La company effettua il subscribe agli eventi di tipo auctionStatus
  system.eventStream.subscribe(self, classOf[auctionStatus])

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

      if (m.open==true){
        //La companyC punta solo all'inizio dell'asta
        if (m.currentWinner==null){

          val r = scala.util.Random
          val myPrice = r.nextInt(1000)
          var myBid = new bidMessage(myPrice+800)
          m.auctioRef ! myBid

        }}else{
        if(m.currentWinner==self){
          myWorks(m.task)=true
          println(s"Evviva! Sono ${self.path.name} e ho vinto l'asta per ${taskNames(m.task)}")
        }}

    case "I'm sorry, i'm dying!" =>
      println("Just few milliseconds...")

    case m: buildingHouse ⇒
      var currentWork:Int = 0
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