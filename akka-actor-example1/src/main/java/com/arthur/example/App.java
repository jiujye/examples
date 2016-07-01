package com.arthur.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinPool;


public class App 
{
    public static void main( String[] args )
    {
    	ActorSystem system = ActorSystem.create("CoffeeShop");

        ActorRef counter = system.actorOf(CoffeeShopWorker.props(),"Counter");
        system.actorOf(new RoundRobinPool(3).props(CoffeeShopWorker.props()), "workerRouter");

        system.actorOf(Customer.props("Latte"),"Customer1");
        system.actorOf(Customer.props("Black Coffee"),"Customer2");
        system.actorOf(Customer.props("Cappuccino"),"Customer3");
        system.actorOf(Customer.props("Iced Coffee"),"Customer4");
        system.actorOf(Customer.props("Iced Tea"),"Customer5");
    }
}
