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

## 3. 获取版本库
* 将当前目录初始化为git版本库  
  `git init`
* 克隆远程版本库  
  `git clone <版本库地址>`

## 4. 查看日志
* 查看提交日志  
  `git log`
* <span id="op_log">查看操作日志</span>  
  `git reflog`

## 5. git add
* `git add <文件名>` 将指定文件纳入到暂存区
* `git add .` 将当前目录下以及当前目录的子目录下的所有未追踪的文件都纳入暂存区

## 6. git rm 与 rm
* git rm:
  * 删除一个文件
  * 将被删除的文件纳入到暂存区（stage, index）
  * 恢复被删除的文件需要进行执行两个命令:
    * `git reset HEAD <文件名>` 将待删除的文件从暂存区恢复到工作区  
      > 作用是：将之前添加到暂存区（stage, index）的内容从暂存区移除到工作区
    * `git checkout -- <文件名>` 将工作区中的修改丢弃掉  
      > 作用是：丢弃掉相对于暂存区中最后一次添加的文件内容所做的变更。
* rm
  * 删除一个文件
  * **没有**将被删除的文件纳入到暂存区（stage, index），可以使用 `git add <文件名>` 将被删除或修改的文件纳入暂存区
* **git mv 与 mv 的区别同上**

## 7. 提交（commit）
* 多行日志  
  `git commit`
* 单行日志  
  `git commit -m '提交日志'`
* `git add .` **+** `git commit -m`（不适用新创建的文件）  
  `git commit --am '提交日志'`
* 修改上一次提交的日志内容  
  * `git commit --amend -m '新的日志内容'`
  * `git commit --amend --reset-author`

## 8. 分支
* 原理
  * 单分支：一个commit对象链，一条工作记录线。
  * HEAD是一个指针，指向当前分支。
  * 分支也是一个指针，指向最近（最后）一次提交。
  * 创建一个分支实际上只创建了一个指针，然后将这个指针指向最近（最后）一次提交。 
  * [了解更多](https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%88%86%E6%94%AF%E7%AE%80%E4%BB%8B)
* 以当前分支最新一次的提交点为基准创建新分支  
  `git branch <分支名>`
* 以指定的提交点为基准创建新分支  
  `git branch <分支名> <commit_id>`
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
* 显示当前分支最近（最后）一次提交的日志  
  `git branch -v`
* 修改分支名称  
  `git branch -m <原分支名> <新分支名>`
 
## 9. 版本回退
* 回退上到一个版本
  * `git reset --hard HEAD^`
  * `git reset --hard HEAD~1`
* 回退或前进到任何一个版本（commit_id可以只写前4个）
  * `git reset --hard <commit_id>`
    > 前进到指定版本时可[查看操作日志](#op_log)来获取commit_id
* HEAD游离状态（'detached HEAD' state）
  * `git checkout <commit_id>`  
    > 此命令可以回退到指定的提交点，但是会处于HEAD游离状态（'detached HEAD' state），此时可以进行一些实验性的变更并且可以提交这些变更，可以通过执行另一个`checkout`命令丢弃掉在当前状态下所进行的任何提交，也可以使用`git checkout -b <新分支名>`命令创建一个新的分支并保留当状态下所创建的任何提交。

## 10. git stash
* 保存工作现场  
  `git stash`
* 保存工作现场并填写日志  
  `git stash save '日志'`
* 查看工作现场列表  
  `git stash list`
* 恢复最后一次保存的工作现场
  * `git stash pop`（恢复的同时也将stash内容删除）
  * `git stash apply`（stash内容并不删除）
* 恢复指定的工作现场  
  `git stash apply stash@{编号}`
* 删除指定的工作现场  
  `git stash drop stash@{编号}`

## 11. Git标签
> 标签有两种：轻量级标签（lightweight）和带有附注标签（annotated）。
* 创建一个轻量级标签  
  `git tag v1.0.1`
* 创建一个带有附注的标签  
  `git tag -a v1.0.2 -m 'release version'`
* 删除标签  
  `git tag -d <tag_name>`
* 查看标签列表  
  `git tag`
* 查找标签
  * 精确查找
    * `git tag -l 'v1.0.1'`
  * 模糊查找
    * `git tag -l *2`
    * `git tag -l v1.*`

## 12. diff
* 显示文件中每一行的commit_id和作者  
  `git blame <文件名>`
* 暂存区与工作区的文件差别  
  `git diff`
* 某一个提交与工作区的差别
  * 最新一次提交与工作区的差别  
    `git diff HEAD`
  * 指定的一次提交与工作区的差别  
    `git diff <commit_id>`
* 某一个提交与暂存区的差别
  * 最新一次提交与暂存区的差别  
    `git diff --cached`
  * 指定的一次提交与暂存区的差别  
    `git diff --cached <commit_id>`
* 两个提交之间的差别  

## 13. 远程操作
* 设置远程仓库  
  `git remote add origin <远程仓库地址>`
* 将本地仓库的某个分支与远程仓库进行关联    
  `git push -u origin <分支名称>` 如：`git push -u origin master`
* 推送  
  `git push`
* 拉取  
  `git pull` 是`fetch`**+**`merge`两个命令的组合

