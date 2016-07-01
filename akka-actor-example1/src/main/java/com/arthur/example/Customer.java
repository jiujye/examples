package com.arthur.example;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import scala.concurrent.duration.Duration;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by arthur on 2016/6/28.
 */
public class Customer extends UntypedActor {



    public static Props props(final String coffee) {
        return Props.create(new Creator<UntypedActor>() {
            private static final long serialVersionUID = 1L;
            public Customer create() throws Exception {
                return new Customer(coffee);
            }
        });
    }

    final String coffee ;
    public Customer(String coffee){
        this.coffee = coffee;
    }


    @Override
    public void preStart() {
        getContext().system().scheduler().scheduleOnce(
                Duration.create(500, TimeUnit.MILLISECONDS),
                getSelf(), new BuyCoffee(this.coffee), getContext().dispatcher(), null);
    }

    @Override
    public void onReceive(Object msg) {

        if (msg instanceof BuyCoffee) {
            BuyCoffee bc = (BuyCoffee)msg;
            getContext().actorSelection("/user/Counter").tell( new OrderRequest(bc.getName()), getSelf());
        }else if(msg instanceof Receipt){
            System.out.println(new Timestamp(new Date().getTime()) + " "+ self().path().name() +" get "+((Receipt)msg).getInfo());
        }else if(msg instanceof Coffee){
            System.out.println(  new Timestamp(new Date().getTime()) + " "+self().path().name() +" get "+((Coffee)msg).getName() );
        }


    }


}
