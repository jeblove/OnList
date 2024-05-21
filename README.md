# OnList

> 是一个私有云，可以用于存储管理自己的文件，适合个人/家庭/小群体使用。
>
> 内容相同的文件实际只会存储一份，根据文件链接记录着用户拥有的文件

---

## 介绍

OnList（Only List）

使用

Springboot+Vue3+MongoDB

文件存储使用MongoDB GridFS

---

### 数据库

- fileLink记录着文件与用户链接的情况；
  可以呈现对应fs.file id、这个文件的MD5和拥有这个文件的用户、文件数。

- user记录着账号的信息，对应的目录id。

- path记录着一个账号所绑定的目录信息，Key为文件名，type对应是0文件或1文件夹，如果是文件则读取fileLinkId、后缀名suffix；如果是文件夹则读取content内容（递归）。

---

### 文件特性

复制文件，会在fileLink更新文件、用户链接数，标记哪些用户拥有多少个该文件；

删除文件，则fileLink-1，链接总数为0时，会真正删除文件。

---

### 安装

#### 数据库

- 具有redis、mongo(gridfs)数据库
  （开发环境mongodb v6.0.5）

使用数据库工具[mongorestore](https://www.mongodb.com/zh-cn/docs/database-tools/installation/installation/)导入初始数据库，输入命令

```bash
mongorestore --uri mongodb://localhost:27017 --db onList --drop --gzip --archive=mongo_init_240521_23-37-18.gz
```

前端

```bash
npm run dev
```

---

测试账号密码

管理员

username: hans2
password: 123456

用户

username: test
password: 123

---



https://github.com/jeblove/OnList.git