import akka.actor.ActorRef

class auctionStatus (var task: Int, var currentBid: Int, var currentWinner: ActorRef, var auctioRef: ActorRef, var open: Boolean)

class bidMessage(var value:Int)

class showWinners ()

class AuctionEndMessage (var auction:ActorRef, var auctionID:Int)

class startWorkMessage (var house:buildingHouse)

class buildingHouse(var status:String, var owner:String, var status2: Int)