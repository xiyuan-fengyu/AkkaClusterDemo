package com.xiyuan.util

import java.io._

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

import ch.ethz.ssh2.{Connection, Session, StreamGobbler}
import org.apache.commons.net.ftp.{FTP, FTPClient, FTPReply}

/**
  * Created by YT on 2016/4/6.
  */
object LinuxUtil {

  def uploadFile(filePath: String, host: String, username: String, password: String, uploadDir: String): Boolean = {
    val ftpClient = new FTPClient
    var fis: FileInputStream = null
    var flag = false

    try {
      ftpClient.connect(host)
      ftpClient.login(username, password)
      if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode)) {
        val uploadFile = new File(filePath)
        fis = new FileInputStream(uploadFile)

        ftpClient.enterLocalPassiveMode()
        ftpClient.changeWorkingDirectory(uploadDir)
        ftpClient.setBufferSize(2048)
        ftpClient.setControlEncoding("UTF-8")

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        //要开启上传写权限，否则无法上传
        //http://blog.sina.com.cn/s/blog_65a38a5b01014efk.html
        flag = ftpClient.storeFile(uploadFile.getName, fis)

        ftpClient.logout()
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      if(fis != null) {
        fis.close()
      }

      try {
        ftpClient.disconnect()
      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
    flag
  }

  private val stdOutTag = "STD_OUT\t"

  private val stdErrorTag = "STD_ERROR\t"

  /**
    * 用ssh执行远程命令
 *
    * @param command linux终端命令
    * @param host
    * @param username
    * @param password
    * @param stdOutBuffer 如果为null表示不关心输出,输出会打印到控制台；如果不为null，则会把输出存入到 stdOutBuffer,用于后续处理，而不会打印到控制台
    */
  def sshExecute(command: String, host: String, username: String, password: String, stdOutBuffer: ArrayBuffer[String] = null): Unit = {
    //http://somefuture.iteye.com/blog/1997459
    var connection: Connection = null
    var session: Session = null
    var stdOutReader: BufferedReader = null
    var stdErrReader: BufferedReader = null
    try {
      connection = new Connection(host)
      connection.connect()
      val connectionSuccess = connection.authenticateWithPassword(username, password)
      if(connectionSuccess) {
        session = connection.openSession()
        session.execCommand(command)
        stdOutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout)))
        stdErrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStderr)))

        val stdOutThd = new StdThread() {
          override def execute() = {
            var stdOutLine: String = null
            var flag = true
            while (flag) {
              stdOutLine = stdOutReader.readLine()
              if(stdOutLine == null) {
                flag = false
              }
              else {
                if (stdOutBuffer != null) {
                  stdOutBuffer += stdOutTag + stdOutLine
                }
                else println(stdOutLine)
              }
            }
          }
        }
        stdOutThd.start()

        val stdErrThd = new StdThread() {
          override def execute() = {
            var stdErrLine: String = null
            var flag = true
            while (flag) {
              stdErrLine = stdErrReader.readLine()
              if(stdErrLine == null) {
                flag = false
              }
              else {
                if (stdOutBuffer != null) {
                  stdOutBuffer += stdErrorTag + stdErrLine
                }
                else println(stdErrLine)
              }
            }
          }
        }
        stdErrThd.start()

        do {
          Thread.sleep(200)
        } while (stdOutThd.isRunning && stdErrThd.isRunning)
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      try {
        if(stdOutReader != null) stdOutReader.close()
        if(stdErrReader != null) stdErrReader.close()
        if(session != null) session.close()
        if(connection != null) connection.close()
      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  /**
    * 查询linux的某个端口是否闲置可用
    *
    * @param port
    * @param host
    * @param username
    * @param password
    * @return
    */
  def isPortFree(port: Int, host: String, username: String, password: String): Boolean = {
    val stdBuffer = new ArrayBuffer[String]()
    LinuxUtil.sshExecute(s"lsof -i:$port", host, username, password, stdBuffer)
    stdBuffer.isEmpty
  }

  private val portRegex = new Regex(".*LISTEN[^0-9]*([0-9]+)/.*")

  /**
    * 杀死暂用某个端口的进程
    * @param port
    * @param host
    * @param username
    * @param password
    */
  def killByPort(port: Int, host: String, username: String, password: String): Unit = {
    val stdBuffer = new ArrayBuffer[String]()
    LinuxUtil.sshExecute(s"netstat -anp | grep '.*:$port.*LISTEN[^0-9]*[0-9]\\+/.*'", host, username, password, stdBuffer)
    stdBuffer.foreach(line => {
      if (line.matches(portRegex.toString())) {
        val matcher = portRegex.pattern.matcher(line)
        if (matcher.find()) {
          val pid = matcher.group(1)
          LinuxUtil.sshExecute(s"kill -9 $pid", host, username, password)
        }
      }
    })
  }

  def mkdirs(dirs: String, host: String, username: String, password: String): Unit = {
    val dirSplit = dirs.split("/")
    var tempDir = ""
    val commandBld = new StringBuilder
    dirSplit.foreach(d => {
      if (d.nonEmpty) {
        tempDir += "/" + d
        commandBld.append(s"mkdir $tempDir;")
      }
    })
    LinuxUtil.sshExecute(commandBld.toString(), host, username, password)
  }

  def pathType(path: String, host: String, username: String, password: String): Int = {
    val stdBuffer = new ArrayBuffer[String]()
    LinuxUtil.sshExecute(s"ls $path -l", host, username, password, stdBuffer)
    if (stdBuffer.nonEmpty) {
      if (stdBuffer(0).startsWith(stdErrorTag + "ls")) -1
      else if (stdBuffer(0).startsWith(stdOutTag + "total")) 1
      else 0
    }
    else -1
  }

  /**
    * 从host利用scp命令复制文件到otherHost，前提：host能够使用 username 无密码登录otherHost
    * @param path
    * @param otherHost
    * @param host
    * @param username
    * @param password
    */
  def scpToOtherLinux(path: String, otherHost: String, host: String, username: String, password: String): Unit = {
    //先在otherHost中创建同样的目录
    //获取path的类型
    val tempPathType = pathType(path, host, username, password)

    if (tempPathType != -1) {
      //在 host 中通过 ssh 命令在 otherHost 中创建相同的文件目录
      val strBld = new StringBuilder

      //拼接创建文件路径的命令
      val pathSplit = path.split("/")
      var tempDicPath = ""
      for (i <- pathSplit.indices) {
        val dicItem = pathSplit(i)
        if (!dicItem.equals("") && (i < pathSplit.length - 1 || tempPathType == 1)) {
          tempDicPath += "/" + dicItem
          strBld.append("ssh " + username + "@" + otherHost + " \"mkdir " + tempDicPath + "\"; ")
        }
      }
      //拼接 scp 命令
      if (tempPathType == 0) {
        strBld.append("scp " + path + " " + otherHost + ":" + tempDicPath)
      }
      else {
        strBld.append("scp -r " + path + " " + otherHost + ":" + tempDicPath.substring(0, tempDicPath.lastIndexOf("/")))
      }

      //执行命令
      sshExecute(strBld.toString(), host, username, password)
    }
  }


  def ls(path: String, host: String, username: String, password: String): Array[String] = {
    val buffer = new ArrayBuffer[String]()
    sshExecute(s"ls $path", host, username, password, buffer)
    val stdOutTagLen = stdOutTag.length
    buffer.filter(_.startsWith(stdOutTag)).map(_.substring(stdOutTagLen)).toArray
  }

}

class StdThread extends Thread {

  private[this] var isRunning_ = false

  override def run() = {
    isRunning_ = true
    execute()
    isRunning_ = false
  }

  def execute() = {
  }

  final def isRunning: Boolean = isRunning_

}