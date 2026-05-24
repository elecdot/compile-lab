#!/usr/bin/env python3
from __future__ import annotations

import html
import re
import shutil
import struct
import zipfile
from dataclasses import dataclass, field
from pathlib import Path


ROOT = Path(__file__).resolve().parent
TEMPLATE = ROOT / "第二组_汇报.pptx"
OUTPUT = ROOT / "第二组_汇报_专业版.pptx"

EMU = 914400
SLIDE_W = 9144000
SLIDE_H = 5143500

BG = "F8FAFC"
NAVY = "0F172A"
SLATE = "334155"
MUTED = "64748B"
LINE = "CBD5E1"
CARD = "FFFFFF"
BLUE = "2563EB"
TEAL = "0F766E"
GREEN = "16A34A"
AMBER = "D97706"
RED = "DC2626"


def emu(value: float) -> int:
    return int(value * EMU)


def esc(value: str) -> str:
    return html.escape(value, quote=True)


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as handle:
        sig = handle.read(24)
    if sig[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"Only PNG images are supported: {path}")
    return struct.unpack(">II", sig[16:24])


def fit_image(path: Path, x: float, y: float, w: float, h: float) -> tuple[int, int, int, int]:
    px_w, px_h = png_size(path)
    box_w = emu(w)
    box_h = emu(h)
    ratio = min(box_w / px_w, box_h / px_h)
    out_w = int(px_w * ratio)
    out_h = int(px_h * ratio)
    out_x = emu(x) + (box_w - out_w) // 2
    out_y = emu(y) + (box_h - out_h) // 2
    return out_x, out_y, out_w, out_h


@dataclass
class ImageRef:
    path: Path
    rel_id: str
    target: str


@dataclass
class Slide:
    title: str
    section: str
    body: list[str] = field(default_factory=list)
    notes: str = ""
    shapes: list[str] = field(default_factory=list)
    images: list[ImageRef] = field(default_factory=list)
    rel_counter: int = 2

    def add_image(self, path: str, target: str | None = None) -> ImageRef:
        image_path = ROOT / path
        rel = ImageRef(image_path, f"rId{self.rel_counter}", target or f"../media/{image_path.name}")
        self.rel_counter += 1
        self.images.append(rel)
        return rel


class DeckBuilder:
    def __init__(self) -> None:
        self.slides: list[Slide] = []
        self.shape_id = 10

    def next_id(self) -> int:
        self.shape_id += 1
        return self.shape_id

    def text_run(self, text: str, size: int, color: str, bold: bool = False) -> str:
        b = ' b="1"' if bold else ""
        return (
            f'<a:r><a:rPr lang="zh-CN" sz="{size * 100}"{b}>'
            f'<a:solidFill><a:srgbClr val="{color}"/></a:solidFill>'
            f'<a:latin typeface="Noto Sans CJK SC"/><a:ea typeface="Noto Sans CJK SC"/>'
            f'</a:rPr><a:t>{esc(text)}</a:t></a:r>'
        )

    def paragraph(
        self,
        text: str,
        size: int = 20,
        color: str = SLATE,
        bold: bool = False,
        bullet: bool = False,
        align: str | None = None,
        space_after: int = 220,
    ) -> str:
        attrs = []
        if bullet:
            attrs.append('marL="228600"')
            attrs.append('indent="-171450"')
        if align:
            attrs.append(f'algn="{align}"')
        attrs.append(f'spAft="{space_after}"')
        ppr = f"<a:pPr {' '.join(attrs)}>"
        if bullet:
            ppr += '<a:buChar char="•"/>'
        ppr += "</a:pPr>"
        return f"<a:p>{ppr}{self.text_run(text, size, color, bold)}</a:p>"

    def text_box(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        paragraphs: list[str],
        fill: str | None = None,
        line: str | None = None,
        radius: bool = False,
    ) -> str:
        sid = self.next_id()
        geom = "roundRect" if radius else "rect"
        fill_xml = (
            f'<a:solidFill><a:srgbClr val="{fill}"/></a:solidFill>'
            if fill
            else '<a:noFill/>'
        )
        line_xml = (
            f'<a:ln w="9525"><a:solidFill><a:srgbClr val="{line}"/></a:solidFill></a:ln>'
            if line
            else '<a:ln><a:noFill/></a:ln>'
        )
        return f"""
        <p:sp>
          <p:nvSpPr><p:cNvPr id="{sid}" name="Text {sid}"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
          <p:spPr>
            <a:xfrm><a:off x="{emu(x)}" y="{emu(y)}"/><a:ext cx="{emu(w)}" cy="{emu(h)}"/></a:xfrm>
            <a:prstGeom prst="{geom}"><a:avLst/></a:prstGeom>{fill_xml}{line_xml}
          </p:spPr>
          <p:txBody><a:bodyPr wrap="square" lIns="91440" tIns="60960" rIns="91440" bIns="60960"/><a:lstStyle/>
            {''.join(paragraphs)}
          </p:txBody>
        </p:sp>
        """

    def rect(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        fill: str,
        line: str | None = None,
        radius: bool = False,
    ) -> str:
        return self.text_box(x, y, w, h, [self.paragraph("", 1, fill)], fill, line, radius)

    def image(self, slide: Slide, rel: ImageRef, x: float, y: float, w: float, h: float) -> str:
        sid = self.next_id()
        out_x, out_y, out_w, out_h = fit_image(rel.path, x, y, w, h)
        return f"""
        <p:pic>
          <p:nvPicPr><p:cNvPr id="{sid}" name="{esc(rel.path.name)}"/><p:cNvPicPr/><p:nvPr/></p:nvPicPr>
          <p:blipFill><a:blip r:embed="{rel.rel_id}"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>
          <p:spPr><a:xfrm><a:off x="{out_x}" y="{out_y}"/><a:ext cx="{out_w}" cy="{out_h}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        </p:pic>
        """

    def footer(self, idx: int, total: int, section: str) -> str:
        return (
            self.text_box(0.45, 7.04 - 0.35, 3.2, 0.25, [self.paragraph(section, 8, MUTED)], None)
            + self.text_box(12.2, 7.04 - 0.35, 0.6, 0.25, [self.paragraph(f"{idx:02d}/{total:02d}", 8, MUTED, align="r")], None)
        )

    def header(self, title: str, section: str) -> str:
        return (
            self.rect(0, 0, 13.333, 0.16, BLUE)
            + self.text_box(0.5, 0.35, 9.2, 0.52, [self.paragraph(title, 22, NAVY, True)], None)
            + self.text_box(10.0, 0.42, 2.7, 0.32, [self.paragraph(section, 9, BLUE, True, align="r")], None)
        )

    def card(self, x: float, y: float, w: float, h: float, title: str, lines: list[str], accent: str = BLUE) -> str:
        paras = [
            self.paragraph(title, 15, accent, True, space_after=100),
            *[self.paragraph(line, 11, SLATE, bullet=True, space_after=120) for line in lines],
        ]
        return self.text_box(x, y, w, h, paras, CARD, LINE, True)

    def metric(self, x: float, y: float, value: str, label: str, color: str) -> str:
        return self.text_box(
            x,
            y,
            2.1,
            1.0,
            [self.paragraph(value, 25, color, True, align="ctr", space_after=60), self.paragraph(label, 9, MUTED, align="ctr")],
            CARD,
            LINE,
            True,
        )

    def flow(self, labels: list[str], y: float = 2.75) -> str:
        pieces = []
        x = 0.72
        w = 1.85
        for i, label in enumerate(labels):
            pieces.append(
                self.text_box(x, y, w, 0.72, [self.paragraph(label, 13, NAVY, True, align="ctr")], "E0F2FE", "93C5FD", True)
            )
            if i < len(labels) - 1:
                pieces.append(self.text_box(x + w + 0.08, y + 0.18, 0.48, 0.32, [self.paragraph("→", 18, BLUE, True, align="ctr")]))
            x += w + 0.64
        return "".join(pieces)

    def slide_xml(self, slide: Slide, idx: int, total: int) -> str:
        shapes = [self.rect(0, 0, 13.333, 7.5, BG), self.header(slide.title, slide.section), *slide.shapes, self.footer(idx, total, slide.section)]
        return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree>
    <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
    <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
    {''.join(shapes)}
  </p:spTree></p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>'''

    def slide_rels(self, slide: Slide) -> str:
        rels = [
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>'
        ]
        for image in slide.images:
            rels.append(
                f'<Relationship Id="{image.rel_id}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="{image.target}"/>'
            )
        return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">{''.join(rels)}</Relationships>'''

    def build(self) -> None:
        self.make_slides()
        with zipfile.ZipFile(TEMPLATE, "r") as src, zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as dst:
            skip_prefixes = ("ppt/slides/", "ppt/media/")
            skip_exact = {"[Content_Types].xml", "ppt/presentation.xml", "ppt/_rels/presentation.xml.rels", "docProps/app.xml"}
            for item in src.infolist():
                if item.filename in skip_exact or item.filename.startswith(skip_prefixes):
                    continue
                dst.writestr(item, src.read(item.filename))

            dst.writestr("[Content_Types].xml", self.content_types(src))
            dst.writestr("ppt/presentation.xml", self.presentation_xml())
            dst.writestr("ppt/_rels/presentation.xml.rels", self.presentation_rels())
            dst.writestr("docProps/app.xml", self.app_xml())

            media_written: dict[Path, str] = {}
            for idx, slide in enumerate(self.slides, start=1):
                dst.writestr(f"ppt/slides/slide{idx}.xml", self.slide_xml(slide, idx, len(self.slides)))
                dst.writestr(f"ppt/slides/_rels/slide{idx}.xml.rels", self.slide_rels(slide))
                for image in slide.images:
                    media_name = image.target.replace("../media/", "ppt/media/")
                    if image.path not in media_written:
                        dst.write(image.path, media_name)
                        media_written[image.path] = media_name

    def content_types(self, src: zipfile.ZipFile) -> str:
        base = src.read("[Content_Types].xml").decode("utf-8")
        base = re.sub(r'<Override PartName="/ppt/slides/slide\d+\.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide\+xml" />', "", base)
        inserts = "".join(
            f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml" />'
            for i in range(1, len(self.slides) + 1)
        )
        return base.replace("</Types>", inserts + "</Types>")

    def presentation_xml(self) -> str:
        slide_ids = "".join(f'<p:sldId id="{255 + i}" r:id="rId{1 + i}"/>' for i in range(1, len(self.slides) + 1))
        return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" saveSubsetFonts="1">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>
  <p:sldIdLst>{slide_ids}</p:sldIdLst>
  <p:sldSz cx="{SLIDE_W}" cy="{SLIDE_H}" type="screen16x9"/>
  <p:notesSz cx="6858000" cy="9144000"/>
  <p:defaultTextStyle/>
</p:presentation>'''

    def presentation_rels(self) -> str:
        rels = [
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>'
        ]
        for i in range(1, len(self.slides) + 1):
            rels.append(f'<Relationship Id="rId{1 + i}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i}.xml"/>')
        start = len(self.slides) + 2
        rels.extend(
            [
                f'<Relationship Id="rId{start}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps" Target="presProps.xml"/>',
                f'<Relationship Id="rId{start + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/viewProps" Target="viewProps.xml"/>',
                f'<Relationship Id="rId{start + 2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>',
                f'<Relationship Id="rId{start + 3}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles" Target="tableStyles.xml"/>',
            ]
        )
        return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">{''.join(rels)}</Relationships>'''

    def app_xml(self) -> str:
        return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Codex</Application><PresentationFormat>On-screen Show (16:9)</PresentationFormat>
  <Slides>{len(self.slides)}</Slides><Notes>0</Notes><HiddenSlides>0</HiddenSlides><MMClips>0</MMClips>
  <ScaleCrop>false</ScaleCrop><HeadingPairs><vt:vector size="2" baseType="variant"><vt:variant><vt:lpstr>幻灯片</vt:lpstr></vt:variant><vt:variant><vt:i4>{len(self.slides)}</vt:i4></vt:variant></vt:vector></HeadingPairs>
  <TitlesOfParts><vt:vector size="{len(self.slides)}" baseType="lpstr">{''.join(f'<vt:lpstr>{esc(s.title)}</vt:lpstr>' for s in self.slides)}</vt:vector></TitlesOfParts>
  <Company></Company><LinksUpToDate>false</LinksUpToDate><SharedDoc>false</SharedDoc><HyperlinksChanged>false</HyperlinksChanged><AppVersion>16.0000</AppVersion>
</Properties>'''

    def new_slide(self, title: str, section: str) -> Slide:
        slide = Slide(title=title, section=section)
        self.slides.append(slide)
        return slide

    def make_slides(self) -> None:
        self.slides = []
        self.shape_id = 10

        s = self.new_slide("编译原理实验汇报：第二组", "Overview")
        s.shapes.extend(
            [
                self.rect(0, 0, 13.333, 7.5, NAVY),
                self.text_box(0.75, 0.9, 7.7, 0.72, [self.paragraph("词法分析、语法分析与三地址代码生成", 25, "FFFFFF", True)]),
                self.text_box(0.78, 1.75, 7.3, 0.5, [self.paragraph("从 Token 到 Parser，再到中间代码的完整前端流程", 15, "CBD5E1")]),
                self.card(0.8, 3.05, 3.55, 1.25, "郑天白", ["词法分析子系统", "Token 与非法整数检测"], "38BDF8"),
                self.card(4.85, 3.05, 3.55, 1.25, "段晰迈", ["递归下降语法分析", "语法树与错误处理"], "34D399"),
                self.card(8.9, 3.05, 3.55, 1.25, "高子涵", ["Bison + AST + TAC", "可视化、优化、MiniSLR"], "FBBF24"),
                self.flow(["Lexer", "Parser", "AST", "TAC"], y=5.55),
            ]
        )

        s = self.new_slide("汇报结构与验证结论", "Basic")
        img = s.add_image("media/build-test-pass.png", "../media/build-test-pass.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.35, 2.65, 1.2, "基本要求", ["词法分析", "语法树输出", "三地址代码生成"], BLUE),
                self.card(3.55, 1.35, 2.65, 1.2, "扩展能力", ["错误定位与续编译", "复合语句与关系运算", "AST/DOT 与优化"], TEAL),
                self.card(6.45, 1.35, 2.65, 1.2, "原理展示", ["Bison 自动生成", "MiniSLR ACTION/GOTO", "LR(0) 自动机"], AMBER),
                self.metric(0.8, 3.1, "20", "自动化 fixture", GREEN),
                self.metric(3.25, 3.1, "3", "成员分工模块", BLUE),
                self.metric(5.7, 3.1, "5+", "扩展展示点", AMBER),
                self.image(s, img, 8.35, 1.35, 4.1, 4.55),
            ]
        )

        s = self.new_slide("词法分析子系统", "Member Work")
        img = s.add_image("lab1-from-teammate/media/image4.png", "../media/lex-state.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.25, 3.25, 1.05, "识别范围", ["标识符与关键字", "三类整数", "运算符与分隔符"], BLUE),
                self.card(0.65, 2.65, 3.25, 1.05, "扩展处理", ["非法八进制", "非法十六进制", "行列位置记录"], TEAL),
                self.card(0.65, 4.05, 3.25, 1.05, "后续作用", ["为 parser 提供 token", "支撑错误定位"], GREEN),
                self.image(s, img, 4.45, 1.05, 7.55, 5.15),
            ]
        )

        s = self.new_slide("递归下降语法分析", "Member Work")
        img1 = s.add_image("lab2-from-teammate/media/image1.png", "../media/parser-s.png")
        img2 = s.add_image("lab2-from-teammate/media/image4.png", "../media/parser-e.png")
        img3 = s.add_image("lab2-from-teammate/media/image8.png", "../media/parser-f.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.25, 3.25, 1.1, "分析方法", ["递归下降", "每个非终结符对应函数"], BLUE),
                self.card(0.65, 2.75, 3.25, 1.1, "文法处理", ["消除左递归", "E' / T' 表示后缀"], TEAL),
                self.card(0.65, 4.25, 3.25, 1.1, "输出结果", ["缩进语法树", "最左派生结构"], GREEN),
                self.image(s, img1, 4.35, 1.08, 7.8, 1.55),
                self.image(s, img2, 4.35, 2.85, 3.65, 1.1),
                self.image(s, img3, 8.2, 2.85, 3.95, 2.65),
            ]
        )

        s = self.new_slide("实验二扩展：错误处理与语言能力", "Member Work")
        img1 = s.add_image("lab2-from-teammate/media/image9.png", "../media/lab2-relop.png")
        img2 = s.add_image("lab2-from-teammate/media/image15.png", "../media/lab2-error.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.22, 2.85, 1.18, "语言扩展", ["六种关系运算符", "begin ... end 复合语句"], BLUE),
                self.card(0.65, 2.82, 2.85, 1.18, "错误处理", ["非法数值定位", "语法错误定位", "记录多个错误"], RED),
                self.card(0.65, 4.42, 2.85, 1.18, "续编译", ["错误后继续分析", "保留后续有效结构"], TEAL),
                self.image(s, img1, 3.95, 1.12, 7.95, 1.9),
                self.image(s, img2, 3.95, 3.2, 7.95, 2.5),
            ]
        )

        s = self.new_slide("Bison 主路径与 AST 中间层", "Integration")
        img = s.add_image("media/lab3-ast-dot.png", "../media/lab3-ast-dot.png")
        s.shapes.extend(
            [
                self.flow(["Lexer", "Bison Parser", "AST", "TacEmitter", "TAC"], y=1.2),
                self.card(0.75, 2.75, 3.2, 1.1, "主路径", ["使用 GNU Bison 生成 Java parser", "不依赖递归下降 parser"], BLUE),
                self.card(0.75, 4.25, 3.2, 1.1, "AST 作用", ["连接语法分析与 TAC", "支持文本/DOT 展示"], TEAL),
                self.image(s, img, 4.45, 2.35, 7.35, 3.55),
            ]
        )

        s = self.new_slide("三地址代码生成", "TAC")
        img = s.add_image("media/lab3-tac-sample.png", "../media/lab3-tac-sample.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.25, 3.05, 1.05, "表达式", ["后序遍历 AST", "生成临时变量 t1/t2"], BLUE),
                self.card(0.65, 2.67, 3.05, 1.05, "控制流", ["生成 L0/L1 标号", "if / goto 组织分支"], TEAL),
                self.card(0.65, 4.09, 3.05, 1.05, "语言结构", ["赋值、if/else", "while 与嵌套结构"], GREEN),
                self.image(s, img, 4.05, 1.3, 7.95, 4.5),
            ]
        )

        s = self.new_slide("错误恢复与常量折叠", "Extensions")
        img1 = s.add_image("media/lab3-error-recovery.png", "../media/lab3-error-recovery.png")
        img2 = s.add_image("media/lab3-tac-opt.png", "../media/lab3-tac-opt.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.25, 3.15, 1.25, "语句级错误恢复", ["Bison error 产生式", "错误语句不生成 TAC", "后续合法语句继续翻译"], RED),
                self.card(0.65, 3.15, 3.15, 1.25, "常量折叠", ["AST 层处理", "默认 TAC 保持原始输出", "优化模式单独启用"], AMBER),
                self.text_box(4.25, 1.15, 7.6, 0.4, [self.paragraph("错误恢复输出", 14, NAVY, True)]),
                self.image(s, img1, 4.25, 1.62, 7.6, 1.25),
                self.text_box(4.25, 3.22, 7.6, 0.4, [self.paragraph("常量折叠输出", 14, NAVY, True)]),
                self.image(s, img2, 4.25, 3.7, 7.6, 1.6),
            ]
        )

        s = self.new_slide("MiniSLR：ACTION/GOTO 与自动机", "Principle")
        img = s.add_image("media/minislr-automaton.png", "../media/minislr-automaton.png")
        s.shapes.extend(
            [
                self.card(0.65, 1.12, 3.2, 1.0, "固定表达式文法", ["E/T/F 经典优先级文法"], BLUE),
                self.card(0.65, 2.5, 3.2, 1.0, "表构造", ["LR(0) item 集", "GOTO 转移", "ACTION/GOTO 表"], TEAL),
                self.card(0.65, 3.88, 3.2, 1.0, "定位", ["parser generator 原理展示", "不替代 Bison 主线"], AMBER),
                self.image(s, img, 4.15, 1.05, 7.95, 5.0),
            ]
        )

        s = self.new_slide("成果小结", "Summary")
        img = s.add_image("media/build-test-pass.png", "../media/build-test-pass.png")
        s.shapes.extend(
            [
                self.card(0.75, 1.25, 3.2, 1.25, "基本流程", ["Lexer", "Parser", "TAC 生成"], BLUE),
                self.card(4.25, 1.25, 3.2, 1.25, "组员扩展", ["非法数值识别", "错误定位与续编译", "关系运算与复合语句"], TEAL),
                self.card(7.75, 1.25, 3.2, 1.25, "Bison/TAC 扩展", ["AST/DOT", "错误恢复", "常量折叠与 MiniSLR"], AMBER),
                self.text_box(0.9, 3.15, 5.4, 1.2, [self.paragraph("边界说明", 17, RED, True), self.paragraph("错误恢复是语句级；MiniSLR 是固定文法原理展示，不是通用 YACC。", 13, SLATE)] , CARD, LINE, True),
                self.image(s, img, 7.2, 3.05, 3.2, 2.75),
            ]
        )


if __name__ == "__main__":
    if not TEMPLATE.exists():
        raise SystemExit(f"Missing template pptx: {TEMPLATE}")
    DeckBuilder().build()
    print(OUTPUT)
