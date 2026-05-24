# 汇报素材清单

Purpose: collect the figures, screenshots, and render targets needed for the
group report and classroom presentation.

## 使用原则

- 组员已经提供的截图优先直接引用原文件，不重复改名复制。
- 需要现场重新截图的内容先保留占位文件名、命令和用途。
- 最终 PPT/Word 中如需统一文件夹，可按本清单把截图导出到
  `docs/reports/media/`。

## 已有组员素材

| 用途 | 来源文件 | 使用位置 |
| --- | --- | --- |
| 词法分析指导书样例运行结果 | `../lab1-from-teammate/media/image1.png` | 报告词法分析小节、PPT 基本要求页 |
| 输入缓冲修改前验证 | `../lab1-from-teammate/media/image2.png` | 报告词法扩展说明 |
| 输入缓冲提交后验证 | `../lab1-from-teammate/media/image3.png` | 报告词法扩展说明 |
| 词法综合状态图 | `../lab1-from-teammate/media/image4.png` | 报告词法状态图小节 |
| 实验二 `S` 语法图 | `../lab2-from-teammate/media/image1.png` | 报告语法分析小节 |
| 实验二 `S'` 语法图 | `../lab2-from-teammate/media/image2.png` | 报告语法分析小节 |
| 实验二 `C` 语法图 | `../lab2-from-teammate/media/image3.png` | 报告语法分析小节 |
| 实验二 `E` 语法图 | `../lab2-from-teammate/media/image4.png` | 报告语法分析小节 |
| 实验二 `E'` 语法图 | `../lab2-from-teammate/media/image5.png` | 报告语法分析小节 |
| 实验二 `T` 语法图 | `../lab2-from-teammate/media/image6.png` | 报告语法分析小节 |
| 实验二 `T'` 语法图 | `../lab2-from-teammate/media/image7.png` | 报告语法分析小节 |
| 实验二 `F` 语法图 | `../lab2-from-teammate/media/image8.png` | 报告语法分析小节 |
| 六种关系运算符验证 | `../lab2-from-teammate/media/image9.png` | 扩展内容页 |
| 复合语句验证 | `../lab2-from-teammate/media/image10.png` | 扩展内容页 |
| 非法数值检测与定位 | `../lab2-from-teammate/media/image11.png` | 错误处理页 |
| 缺分号隐式纠错验证 | `../lab2-from-teammate/media/image12.png` | 错误处理页 |
| 缺表达式续编译验证 | `../lab2-from-teammate/media/image13.png` | 错误处理页 |
| 非法整数错误定位验证 | `../lab2-from-teammate/media/image14.png` | 错误处理页 |
| 混合错误续编译验证 | `../lab2-from-teammate/media/image15.png` | 错误处理页 |
| 综合关系符测试 | `../lab2-from-teammate/media/image16.png` | 扩展内容页 |

## 待截图素材

| 占位文件名 | 内容 | 采集命令或来源 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `build-test-pass.png` | 20 个 fixture 全部通过 | `make test` | 基本要求验证页、结尾页 |
| `lab3-tac-sample.png` | 指导书样例 TAC 输出 | `java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in` | 三地址代码生成页 |
| `lab3-error-recovery.png` | Bison TAC 路径语句级错误恢复 | `java -cp build/classes Experiment2 --tac < tests/lab3_tac_error_multiple.in` | 错误恢复扩展页 |
| `lab3-ast-output.png` | Bison 路径 AST 文本展示 | `java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in` | AST 中间层页 |
| `lab3-ast-dot.png` | AST DOT 渲染图 | `java -cp build/classes Experiment2 --ast-dot < tests/lab3_ast_dot_sample.in` 后用 Graphviz 渲染 | AST 可视化页 |
| `lab3-tac-opt.png` | 常量折叠优化输出 | `java -cp build/classes Experiment2 --tac-opt < tests/lab3_tac_constant_folding.in` | 优化扩展页 |
| `minislr-automaton.png` | MiniSLR LR(0) 自动机图 | `java -cp build/classes MiniSlrDemo --dot` 后用 Graphviz 渲染 | MiniSLR DOT 可视化页 |

## 建议 PPT 素材顺序

1. `build-test-pass.png`
2. 词法分析样例截图：`../lab1-from-teammate/media/image1.png`
3. 词法状态图：`../lab1-from-teammate/media/image4.png`
4. 实验二语法图：优先选 `S`、`E`、`T`、`F`
5. `lab3-tac-sample.png`
6. `lab3-error-recovery.png`
7. `lab3-ast-dot.png`
8. `lab3-tac-opt.png`
9. `minislr-automaton.png`
