import akka.actor.ActorRef

class casa(var status:String)

class workStartMessage (var house:casa)


class auctionStatus (var task: Int, var currentBid: Int, var currentWinner: ActorRef, var auctioRef: ActorRef)

class killMessage(var dyingActor:ActorRef)

class bidMessage(var value:Int){

}

class AuctionMessage (var obj: String, var resPrice: Int, var ref: ActorRef, var objnumber:Int){

}

class AuctionEndMessage (var auction:ActorRef, var auctionID:Int) {

}

class showWinners () {

}

class AuctionSubscribeMessage(){

}

class AuctionStartMessage(var duration:Int){

}

class StartBiddingMessage (var obj: String, var resPrice: Int, var ref: ActorRef, var objnumber:Int){

}

class StopBidMessage{

}

class WinnerMessage(var winner: ActorRef, var value: Int, var obj:String){

}

class StartWork(var work:Int, var lavori:Array[String]){

}

class WorkCompleted(var work:Int){

}

class winnerCongrats(var auctionID:Int){

}