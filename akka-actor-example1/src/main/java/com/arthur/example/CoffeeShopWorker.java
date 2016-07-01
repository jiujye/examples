package com.arthur.example;

import akka.actor.ActorRef;
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
public class CoffeeShopWorker extends  UntypedActor {


    public static Props props() {
        return Props.create(new Creator<UntypedActor>() {
            private static final long serialVersionUID = 1L;
            public CoffeeShopWorker create() throws Exception {
                return new CoffeeShopWorker();
            }
        });
    }

    @Override
    public void preStart() {
        getContext().system().scheduler().scheduleOnce(
                Duration.create(15000, TimeUnit.MILLISECONDS),
                getSelf(), "close", getContext().dispatcher(), null);
    }

    @Override
    public void onReceive(Object msg) {

        if (msg instanceof OrderRequest ) {
            System.out.println(new Timestamp(new Date().getTime()) + " Counter handle " + sender().path().name() + " order.");
            OrderRequest req = (OrderRequest)msg;
            sender().tell(new Receipt(),ActorRef.noSender());
            getContext().actorSelection("/user/workerRouter")
                            .tell(new MakeCoffee(req.getRequest(),sender().path().toString()),
                                    ActorRef.noSender());
        }else if(msg instanceof  MakeCoffee){
            MakeCoffee  mc = (MakeCoffee)msg;
            System.out.println(new Timestamp(new Date().getTime()) + " Worker "+ self().path().name() +" make " + mc.getCoffee() +".");
            getContext().actorSelection(mc.getCustomerPath())
                    .tell(new Coffee(mc.getCoffee()), ActorRef.noSender());
        }else if( msg instanceof  String){
            if("close".equals(msg.toString())){
                getContext().system().shutdown();
            }
        }


    }

}
