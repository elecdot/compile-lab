# 课堂演示脚本

用途：为课堂汇报准备一套简短、可复现的演示流程。正常汇报时优先使用截图；只有老师要求现场验证时，再运行本文档中的命令。

## 演示原则

- 总时长控制在 3 分钟内。
- 优先展示截图，不现场输入长程序。
- 现场命令只选 2 到 3 条，避免时间失控。
- 如果现场环境不稳定，直接使用 `docs/reports/media/` 中已经准备好的截图。

## 演示前准备

在项目根目录执行：

```sh
make build
```

确认 Java class 已生成：

```sh
ls build/classes
```

## 推荐现场流程

### 1. 自动化测试验证

命令：

```sh
make test
```

说明：

```text
这一步用于证明词法分析、实验二语法树、实验三 TAC、错误恢复、AST/DOT、优化和 MiniSLR 展示都已经纳入测试。当前共有 20 个 fixture。
```

预期关注点：

- 输出中出现 `ok ...`；
- 最后一项为 `ok minislr_dot`；
- 对应截图：`docs/reports/media/build-test-pass.png`。

### 2. 三地址代码生成

命令：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in
```

说明：

```text
这一步展示实验三核心结果：源程序经过 Bison parser 构造 AST，再由 TAC 后端生成三地址代码。
```

预期关注点：

- 表达式被拆成临时变量；
- `while`、`if` 结构生成标号和跳转；
- 对应截图：`docs/reports/media/lab3-tac-sample.png`。

### 3. 错误恢复

命令：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_error_multiple.in
```

说明：

```text
这一步展示错误处理扩展。错误语句会报告位置并跳过，后续合法语句仍然可以继续生成 TAC。
```

预期关注点：

- 输出多个 `语法错误 [行...列...]`；
- 最后一条合法语句仍生成 TAC；
- 对应截图：`docs/reports/media/lab3-error-recovery.png`。

## 备选演示

### AST 展示

命令：

```sh
java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in
```

用途：

```text
如果需要说明为什么使用 AST 中间层，可以展示该命令输出。
```

对应截图：

```text
docs/reports/media/lab3-ast-output.png
docs/reports/media/lab3-ast-dot.png
```

### 常量折叠

命令：

```sh
java -cp build/classes Experiment2 --tac-opt < tests/lab3_tac_constant_folding.in
```

用途：

```text
展示 AST 层常量折叠，说明三地址代码生成前可以做简单优化。
```

对应截图：

```text
docs/reports/media/lab3-tac-opt.png
```

### MiniSLR 自动机

命令：

```sh
java -cp build/classes MiniSlrDemo --dot
```

用途：

```text
展示固定表达式文法的 LR(0) 状态自动机 DOT 输出。PPT 中建议直接放渲染后的图片，不建议现场展示完整 DOT 文本。
```

对应截图：

```text
docs/reports/media/minislr-automaton.png
```

## 3 分钟讲解串词

```text
我们组按编译前端流程分工：词法分析、递归下降语法分析、Bison 语法分析和三地址代码生成。

基本要求方面，词法、语法树和三地址代码都有对应输出，自动化测试覆盖 20 个 fixture。

扩展方面，词法和实验二部分加入了非法数值识别、错误定位、关系运算、复合语句和续编译。

实验三部分使用 Bison 生成 parser，通过 AST 中间层生成 TAC，并补充了错误恢复、常量折叠、AST/DOT 可视化和 MiniSLR 自动机展示。

需要说明的是，错误恢复是语句级恢复，MiniSLR 是固定文法原理展示，不是通用 YACC。
```

## 异常处理

如果现场 `make test` 过长或终端显示不完整：

1. 直接展示 `docs/reports/media/build-test-pass.png`。
2. 只运行 `Experiment2 --tac` 或错误恢复中的一条命令。
3. 说明完整测试已在报告中记录。
