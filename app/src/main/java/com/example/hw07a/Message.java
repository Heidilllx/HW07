package com.example.hw07a;

import java.util.Date;

public class Message {

    String firstName, lastName;
    Date date;
    String messageId;
    String image;
    String myText;

    @Override
    public String toString() {
        return "Message{" +
                "myText='" + myText + '\'' +
                ", firstName='" + firstName + '\'' +
                ", LastName='" + lastName + '\'' +
                ", date=" + date +
                ", image='" + image + '\'' +
                ", messageID='" + messageId + '\'' +
                '}';
    }

}
