package com.arthur.example;

/**
 * Created by arthur on 2016/6/29.
 */
public class Product {

    static public class Latte {
        private final int price;

        public Latte(int price) {
            this.price = price;
        }

        public int getGreeter() {
            return price;
        }
    }

    static public class Greeting {
        private final String from;

        public Greeting(String from) {
            this.from = from;
        }

        public String getGreeter() {
            return from;
        }
    }
}
