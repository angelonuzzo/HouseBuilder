import akka.actor.{ActorSystem, Props}


object main extends App {

  /*
  val r = scala.util.Random

  // Spostare sulla conoscenza dell'acutioneer
  val lavori=Array[String]("Site_preparation","Floors","Walls","Roof","Windows","Doors","Plumbing","Electric_System","paint1","Paint2")





  //Skills sono assegnate a priori e non random

  var counter2:Int= 0
  var altroCounter:Int=0
  var IntrestedArray:Array[String] = new Array[String](5)
  IntrestedArray(0)="bla"
  IntrestedArray(1)="bla"
  IntrestedArray(2)="bla"
  var full : Boolean=false
  var already:Boolean=false
  val NumeroCompany:Int=15
  var n:Int=0
  var assegnati:Int=0

*/
  //Creo il sistema di agenti
  val system: ActorSystem = ActorSystem.create("test-system")
  println("Sto inizializzando le companies")

  // Creo tutte le companies
  val CompanyA = system.actorOf(Props(classOf[CompanyA]),"CompanyA")
  val CompanyB = system.actorOf(Props(classOf[CompanyB]),"CompanyB")
  for (i<-1 to 5){
    val CompanyC = system.actorOf(Props(classOf[CompanyC]),s"CompanyC-${i}")
  }
  for (i<-1 to 13){
    val CompanyD = system.actorOf(Props(classOf[CompanyD]),s"CompanyD-${i}")
  }
  val CompanyE = system.actorOf(Props(classOf[CompanyE]),"CompanyE")


  println("Tutte le companies sono state inizializzate")


  // Credo l'attore Auctioneer
  val Auctioneer = system.actorOf(Props(classOf[Auctioneer]), "Auctioneer")

  // Gli invio il messaggio di start
  Auctioneer ! "start"





}
