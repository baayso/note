# Git

## 1. Git的提交id（commit id）
* 是一个摘要值，使用sha1算法计算得到的。
* 不同于SVN这样的集中式版本管理系统使用数字递增，分布式版本管理系统由于不是集中管理版本所以无法使用数字递增来做提交id。

## 2. user.name 与 user.email，可以设置的地方：
* /etc/gitconfig （针对一台电脑上所有用户，不常用，优化级最低）
  * `git config --system [--unset] user.name <Username>`
  * `git config --system [--unset] user.email <Email>`
* ~/.gitconfig （针对某一用户下所有git仓库，常用，优化级其次）
  * `git config --global [--unset] user.name <Username>`
  * `git config --global [--unset] user.email <Email>`
* .git/config （针对某一git仓库，常用，优先级最高）
  * `git config --local [--unset] user.name <Username>`
  * `git config --local [--unset] user.email <Email>`
> 注：--unset 移除配置

## 3. git add
* `git add <文件名>` 将指定文件纳入到暂存区
* `git add .` 将当前目录下以及当前目录的子目录下的所有文件都纳入暂存区

## 4. git rm 与 rm
* git rm:
  * 删除一个文件
  * 将被删除的文件纳入到暂存区（stage, index）
  * 恢复被删除的文件需要进行执行两个命令:
    * `git reset HEAD <文件名>` 将待删除的文件从暂存区恢复到工作区
    * `git checkout -- <文件名>` 将工作区中的修改丢弃掉
* rm
  * 删除一个文件
  * **没有**将被删除的文件纳入到暂存区（stage, index），可以使用 `git add <文件名>` 将被删除或修改的文件纳入暂存区
* **git mv 与 mv 的区别同上**

## 5. 修改上一次提交的日志内容
* `git commit --amend -m '新的日志内容'`

## 6. 分支
* 创建分支  
  `git branch <分支名>`
* 切换分支  
  * `git checkout <分支名>`
  * `git checkout -` 切换到上一次所在的分支
* 创建分支并切换分支到新创建的分支  
  `git checkout -b <分支名>`
* 删除分支（不允许删除当前分支）
  * `git branch -d <分支名>` 不可以删除未合并代码的分支
  * `git branch -D <分支名>` 可以删除未合并代码的分支
* 合并分支  
  `git merge <分支名>` 将其他分支上的修改合并（应用）到当前分支上
* 显示当前分支最近一次提交的日志  
  `git branch -v`
