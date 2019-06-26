package com.pq.rpc.transport.constance;

/**
 * 网络通信层的一些常量
 *
 * @author pengqi
 * create at 6/25
 */
public class CommunicationConstant {

    public static final int MAX_FRAME_LENGTH = 1024*1024;   //单个包的最大长度为1K

    public static final int LENGTH_FIELD_LENGTH = 4;        //长度字段字节数

    public static final int LENGTH_FIELD_OFFSET = 0;        //长度域偏移量

    public static final int LENGTH_ADJUSTMENT = 0;          //数据字段相对于长度域的偏移量

    public static final int INITIAL_BYTES_TO_STRIP = 4;     //从整个包的第一个字节开始,向后忽略4个字节

}
