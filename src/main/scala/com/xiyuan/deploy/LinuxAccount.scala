package com.xiyuan.deploy

import scala.collection.mutable

/**
  * Created by xiyuan_fengyu on 2016/7/18.
  */
object LinuxAccount {

  private val accounts = Map[String, Account](
    "192.168.1.240" -> new Account("root", "111111"),
    "192.168.1.241" -> new Account("root", "111111"),
    "192.168.1.242" -> new Account("root", "111111"),
    "192.168.1.243" -> new Account("root", "111111"),
    "192.168.1.244" -> new Account("root", "111111"),
    "192.168.1.245" -> new Account("root", "111111"),
    "192.168.1.246" -> new Account("root", "111111"),
    "192.168.1.250" -> new Account("root", "111111")
  )

  def apply(host: String): Account = {
    if (accounts.contains(host)) accounts(host)
    else null
  }

}

case class Account(val username: String, val password: String)
