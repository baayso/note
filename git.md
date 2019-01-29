# Git

## 1. Git的提交id（commit id）
* 是一个摘要值，使用sha1算法计算得到的。
* 不同于SVN这样的集中式版本管理系统使用数字递增，分布式版本管理系统由于不是集中管理版本所以无法使用数字递增来做提交id。

## 2. user.name 与 user.email，可以设置的地方：
* /etc/gitconfig （针对一台电脑上所有用户，不常用，优化级最低）
  * git config --system [--unset] user.name \<Username>
  * git config --system [--unset] user.email \<Email>
* ~/.gitconfig （针对某一用户下所有git仓库，常用，优化级其次）
  * git config --global [--unset] user.name \<Username>
  * git config --global [--unset] user.email \<Email>
* .git/config （针对某一git仓库，常用，优先级最高）
  * git config --local [--unset] user.name \<Username>
  * git config --local [--unset] user.email \<Email>
> 注：--unset 移除配置
