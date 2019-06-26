package com.pq.rpc.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息对象,封装了RPC请求对象/RPC响应对象以及消息类型
 *
 * @author pengqi
 * create at 2019/6/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private RPCRequest request;

    private RPCResponse response;

    private byte type;

    public Message(byte type){
        this.type = type;
    }

    public static Message buildRequest(RPCRequest request){
        return new Message(request,null,REQUEST);
    }

    public static Message buildResponse(RPCResponse response){
        return new Message(null,response,RESPONSE);
    }

    //所有消息类型
    public static final byte PING = 1;          //PING消息,用于心跳检测
    public static final byte PONG = 1<<1;       //PONG消息,用于心跳检测
    public static final byte REQUEST = 1<<2;    //请求消息
    public static final byte RESPONSE = 1<<3;   //响应消息
    public static final Message PING_MSG = new Message(PING);   //PING消息对象
    public static final Message PONG_MSG = new Message(PONG);   //PONG消息对象
}
