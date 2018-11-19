import akka.actor.ActorRef

case class auctionStatus (var task: Int, var currentBid: Int, var currentWinner: ActorRef, var auctioRef: ActorRef, var open: Boolean)

case class bidMessage(var value:Int)

case class showWinners ()

case class AuctionEndMessage (var auction:ActorRef, var auctionID:Int)

case class startWorkMessage (var house:buildingHouse)

case class buildingHouse(var status:String, var owner:String, var status2: Int)