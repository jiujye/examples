package com.arthur.example;

/**
 * Created by arthur on 2016/6/29.
 */
public class MakeCoffee {

    private final String coffee;
    private final String customerPath;

    public MakeCoffee(String coffee, String customerPath) {
        this.coffee = coffee;
        this.customerPath = customerPath;
    }

    public String getCoffee() {
        return coffee;
    }

    public String getCustomerPath() {
        return customerPath;
    }



}
