package com.pbu.sam.exceptions;

public class WendiThreadCanceledException extends InterruptedException {
    public WendiThreadCanceledException(){
        super("Transaction has been canceled by user");
    }
}
