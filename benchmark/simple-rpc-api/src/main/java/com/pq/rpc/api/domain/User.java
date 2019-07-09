package com.pq.rpc.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户类
 *
 * @author pengqi
 * create at 2019/7/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private String userName;

}
