from pathlib import Path
import re, shutil, textwrap, json
from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.shared import Cm, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(r"C:\Users\timur\Downloads\software-store-web\software-store-web")
OUT = Path(r"F:\SoftStore-course-document")
ASSETS = OUT / "assets"
OUT.mkdir(parents=True, exist_ok=True); ASSETS.mkdir(parents=True, exist_ok=True)
REPORT = ROOT / "tmp/course-note/report.md"

FONT_PATHS = [r"C:\Windows\Fonts\times.ttf", r"C:\Windows\Fonts\arial.ttf"]
FONT_FILE = next((p for p in FONT_PATHS if Path(p).exists()), None)
def font(size, bold=False):
    path = r"C:\Windows\Fonts\timesbd.ttf" if bold and Path(r"C:\Windows\Fonts\timesbd.ttf").exists() else FONT_FILE
    return ImageFont.truetype(path, size)

def wrap(draw, text, f, width):
    out=[]
    for paragraph in text.split("\n"):
        line=""
        for word in paragraph.split():
            trial=(line+" "+word).strip()
            if draw.textlength(trial,font=f) <= width: line=trial
            else: out.append(line); line=word
        if line: out.append(line)
    return out or [""]

def diagram(name, title, packages=None, flow=None):
    img=Image.new("RGB",(1800,1100),"white"); d=ImageDraw.Draw(img)
    d.text((900,45),title,font=font(39,True),fill="#111111",anchor="ma")
    if packages:
        cols=len(packages); w=1540//cols; y=250
        for i,(head,items,color) in enumerate(packages):
            x=130+i*w
            d.rounded_rectangle((x,y,x+w-50,y+470),radius=24,outline="#23395d",width=4,fill="#f6f9fc")
            d.rounded_rectangle((x,y,x+w-50,y+78),radius=24,fill=color)
            d.text((x+(w-50)//2,y+39),head,font=font(28,True),fill="white",anchor="mm")
            yy=y+115
            for it in items:
                d.rounded_rectangle((x+25,yy,x+w-75,yy+57),radius=8,outline="#8aa4c4",width=2,fill="white")
                d.text((x+(w-50)//2,yy+29),it,font=font(20),fill="#111",anchor="mm")
                yy+=76
        for i in range(cols-1):
            x=130+(i+1)*w-56; d.line((x,y+235,x+64,y+235),fill="#1f4e79",width=6)
            d.polygon([(x+64,y+235),(x+44,y+223),(x+44,y+247)],fill="#1f4e79")
    if flow:
        n=len(flow); margin=120; boxw=(1560-(n-1)*38)//n; y=390
        for i,(head,body,color) in enumerate(flow):
            x=margin+i*(boxw+38)
            d.rounded_rectangle((x,y,x+boxw,y+230),radius=28,outline="#234",width=4,fill="#f8fbff")
            d.rounded_rectangle((x,y,x+boxw,y+64),radius=28,fill=color)
            d.text((x+boxw//2,y+32),head,font=font(26,True),fill="white",anchor="mm")
            yy=y+92
            for ln in wrap(d,body,font(20),boxw-36): d.text((x+18,yy),ln,font=font(20),fill="#111"); yy+=29
            if i<n-1:
                ax=x+boxw; d.line((ax,y+115,ax+38,y+115),fill="#1f4e79",width=5); d.polygon([(ax+38,y+115),(ax+24,y+105),(ax+24,y+125)],fill="#1f4e79")
    path=ASSETS/name; img.save(path); return path

def tree_diagram(name,title,root,branches):
    img=Image.new("RGB",(1800,1100),"white"); d=ImageDraw.Draw(img)
    d.text((900,45),title,font=font(39,True),fill="#111",anchor="ma")
    d.rounded_rectangle((650,135,1150,220),radius=18,fill="#1f4e79"); d.text((900,177),root,font=font(30,True),fill="white",anchor="mm")
    ys=[360,680]; xs=[210,690,1170]
    for idx,(head,items) in enumerate(branches):
        x=xs[idx%3]; y=ys[idx//3]
        d.line((900,220,x+210,y),fill="#1f4e79",width=4)
        d.rounded_rectangle((x,y,x+420,y+270),radius=18,outline="#1f4e79",width=3,fill="#f7faff")
        d.rounded_rectangle((x,y,x+420,y+58),radius=18,fill="#4f81bd")
        d.text((x+210,y+29),head,font=font(23,True),fill="white",anchor="mm")
        yy=y+85
        for item in items:
            for ln in wrap(d,item,font(19),370): d.text((x+24,yy),ln,font=font(19),fill="#111"); yy+=26
            yy+=8
    path=ASSETS/name; img.save(path); return path

def set_cell_shading(cell, color):
    tcPr=cell._tc.get_or_add_tcPr(); shd=OxmlElement('w:shd'); shd.set(qn('w:fill'),color); tcPr.append(shd)
def set_cell_margin(cell, top=80, start=100, bottom=80, end=100):
    tc=cell._tc; tcPr=tc.get_or_add_tcPr(); mar=tcPr.first_child_found_in('w:tcMar')
    if mar is None: mar=OxmlElement('w:tcMar'); tcPr.append(mar)
    for side,val in [('top',top),('start',start),('bottom',bottom),('end',end)]:
        node=mar.find(qn('w:'+side))
        if node is None: node=OxmlElement('w:'+side); mar.append(node)
        node.set(qn('w:w'),str(val)); node.set(qn('w:type'),'dxa')
def set_repeat_table_header(row):
    trPr=row._tr.get_or_add_trPr(); el=OxmlElement('w:tblHeader'); el.set(qn('w:val'),'true'); trPr.append(el)
def add_page_field(paragraph):
    run=paragraph.add_run(); fld=OxmlElement('w:fldSimple'); fld.set(qn('w:instr'),'PAGE'); run._r.append(fld)

def configure_section(sec):
    sec.top_margin=Cm(2); sec.bottom_margin=Cm(2); sec.left_margin=Cm(3); sec.right_margin=Cm(1.5); sec.header_distance=Cm(1.25); sec.footer_distance=Cm(1.25)

def set_run(run, size=14, bold=False, italic=False, font_name='Times New Roman'):
    run.font.name=font_name; run._element.rPr.rFonts.set(qn('w:eastAsia'),font_name); run.font.size=Pt(size); run.bold=bold; run.italic=italic

def add_para(doc,text='',style=None,align=None,first=True,space_after=0):
    p=doc.add_paragraph(style=style)
    if align is not None: p.alignment=align
    pf=p.paragraph_format; pf.line_spacing=1.5; pf.space_after=Pt(space_after)
    if first: pf.first_line_indent=Cm(1.25)
    r=p.add_run(text); set_run(r); return p

def add_heading(doc,text,level):
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER if level==1 else WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before=Pt(14 if level==1 else 10); p.paragraph_format.space_after=Pt(8); p.paragraph_format.keep_with_next=True
    r=p.add_run(text); set_run(r,16 if level==1 else 14 if level==2 else 14,True)
    return p

def add_table(doc, rows):
    table=doc.add_table(rows=0, cols=len(rows[0])); table.alignment=WD_TABLE_ALIGNMENT.CENTER; table.style='Table Grid'
    for ri,row in enumerate(rows):
        cells=table.add_row().cells
        for ci,value in enumerate(row):
            cell=cells[ci]; cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER; set_cell_margin(cell)
            cell.text=''
            p=cell.paragraphs[0]; p.paragraph_format.space_after=Pt(0); p.paragraph_format.line_spacing=1.0
            r=p.add_run(value); set_run(r,10 if len(value)>80 else 11,bold=(ri==0))
            if ri==0: set_cell_shading(cell,'1F4E79'); r.font.color.rgb=RGBColor(255,255,255); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
            elif ci==0: set_cell_shading(cell,'EAF1F8')
        if ri==0: set_repeat_table_header(table.rows[-1])
    doc.add_paragraph().paragraph_format.space_after=Pt(3)

def add_image(doc,path,caption,width=Cm(15.5)):
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.paragraph_format.space_before=Pt(6); p.paragraph_format.space_after=Pt(3); p.add_run().add_picture(str(path),width=width)
    c=doc.add_paragraph(); c.alignment=WD_ALIGN_PARAGRAPH.CENTER; c.paragraph_format.space_after=Pt(8); r=c.add_run(caption); set_run(r,11,italic=True)

def glossary():
    rows=[['Термин','Определение'],['API','Интерфейс программирования приложений; в проекте — HTTP JSON API.'],['Authentication','Подтверждение личности пользователя по JWT.'],['Authorization','Проверка прав USER или ADMIN.'],['BCrypt','Алгоритм хеширования пароля.'],['Cart','Серверная корзина пользователя.'],['CartItem','Позиция корзины с продуктом и количеством.'],['CheckoutFacade','Посредник, координирующий заказ, оплату и лицензии.'],['DTO','Объект обмена данными на REST-границе.'],['EntityGraph','Точечный граф загрузки JPA-ассоциаций.'],['Flyway','Инструмент версионирования SQL-миграций.'],['Foundation','Слой repositories и инфраструктуры доступа к данным.'],['Hibernate','ORM-провайдер JPA.'],['Identity Map','Уникальность сущностей в Persistence Context.'],['JWT','Подписанный токен аутентификации.'],['Lazy Load','Отложенная загрузка связей Entity.'],['License','Демонстрационный лицензионный ключ.'],['Mediator','Слой правил и транзакционных сценариев.'],['Order','Заказ пользователя.'],['OrderItem','Позиция заказа с snapshot цены.'],['PCMEF','Presentation Control Mediator Entity Foundation.'],['REST','Архитектурный стиль HTTP-интерфейса.'],['Swagger UI','Интерактивный просмотр OpenAPI.'],['Transaction','Атомарная операция БД.'],['WAR','Архив для сервлет-контейнера или java -jar.']]
    return rows

def tests_table():
    return [['Класс тестов','Количество','Основная проверка'],['CartMediatorTest','9','Добавление, количество, удаление, сумма'],['CategoryMediatorTest','5','Создание, дубликат, изменение, конфликт удаления'],['CheckoutFacadeTest','7','Создание, оплата, история, запрет PAID через admin'],['LicenseMediatorTest','1','Количество лицензий'],['PaymentMediatorTest','1','Сохранение demo-платежа'],['ProductMediatorTest','3','Создание, изменение, архивирование'],['UserMediatorTest','4','Регистрация, дубликат, вход, неверный пароль'],['ApiSecurityMvcTest','9','Public API, 401/403, validation'],['PageControllerMvcTest','3','Главная, auth, catalog'],['Итого','42','Failures 0, Errors 0']]

def matrix():
    return [['Требование методических указаний','Фактическое состояние','Статус'],['Web UI: минимум 5 страниц','9 Thymeleaf-страниц','Выполнено'],['Адаптивность, JS validation, AJAX','CSS media rules, client validation, Fetch API','Выполнено'],['Роли, BCrypt, JWT','ROLE_USER/ROLE_ADMIN, BCrypt, JWT','Выполнено'],['XSS и SQL injection','textContent, JPA/Spring Data параметры','Частично: без отдельного security-аудита'],['REST: минимум 8 операций','25 комбинаций метода и маршрута v1','Выполнено'],['OpenAPI','springdoc и Swagger UI','Выполнено'],['PostgreSQL и миграции','PostgreSQL 16, Flyway V1/V2','Выполнено'],['3НФ','Нормализован каталог; лицензия хранит избыточный user_id','Частично'],['PCMEF и отсутствие циклов','Пакеты и IService; нет импортов F в P/C/E','Частично: JwtAuthFilter → UserRepository'],['JUnit и coverage более 40%','42 теста, line 62,69%','Выполнено'],['Checkstyle','0 violations','Выполнено'],['WAR и инструкция Tomcat','Executable WAR и docs','Выполнено; external Tomcat не запускался'],['Docker Compose и БД','app + db + healthcheck','Выполнено'],['Git-статистика в README','Нет .git и снимков Insights в текущей копии','Не подтверждено']]

def methods():
    return [['Интерфейс','Ключевые операции'],['IUserService','register, login, findIdByEmail'],['IProductService','all, get, create, update, archive'],['ICategoryService','all, get, create, update, delete'],['ICartService','getCart, addItem, updateItem, removeItem, clearCart'],['IOrderService','createFromCart, history, getOwn, pay, updateStatus'],['IPaymentService','process'],['ILicenseService','issueFor, findByUserEmail']]

def code_file(rel, caption):
    path=ROOT/rel; txt=path.read_text(encoding='utf-8'); return caption,txt

def setup_styles(doc):
    styles=doc.styles
    normal=styles['Normal']; normal.font.name='Times New Roman'; normal._element.rPr.rFonts.set(qn('w:eastAsia'),'Times New Roman'); normal.font.size=Pt(14)
    for n in ['Heading 1','Heading 2','Heading 3']:
        s=styles[n]; s.font.name='Times New Roman'; s._element.rPr.rFonts.set(qn('w:eastAsia'),'Times New Roman'); s.font.color.rgb=RGBColor(0,0,0)

def cover(doc):
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
    for line in ['МИНИСТЕРСТВО НАУКИ И ВЫСШЕГО ОБРАЗОВАНИЯ','РОССИЙСКОЙ ФЕДЕРАЦИИ','ФГАОУ ВО «СЕВЕРО-КАВКАЗСКИЙ ФЕДЕРАЛЬНЫЙ УНИВЕРСИТЕТ»','ИНСТИТУТ ЦИФРОВОГО РАЗВИТИЯ','МЕЖИНСТИТУТСКАЯ БАЗОВАЯ КАФЕДРА']:
        r=p.add_run(line+'\n'); set_run(r,13,True)
    for _ in range(8): doc.add_paragraph()
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('КУРСОВОЙ ПРОЕКТ'); set_run(r,16,True)
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('по дисциплине «Программная инженерия»'); set_run(r,14)
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('на тему\n«Разработка веб-приложения по продаже программного обеспечения»'); set_run(r,15,True)
    for _ in range(6): doc.add_paragraph()
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.LEFT; p.paragraph_format.left_indent=Cm(9)
    for line in ['Выполнил:','Курбанов Тимур Магомедович','студент 3 курса','группы ПИЖ-б-о-23-1(1)','направления 09.03.04 «Программная инженерия»','профиль «Разработка и сопровождение программного обеспечения»','','Руководитель проекта:','С. В. Свистунов, доцент межинститутской базовой кафедры']:
        r=p.add_run(line+'\n'); set_run(r,12)
    for _ in range(4): doc.add_paragraph()
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('Ставрополь, 2026 г.'); set_run(r,13)
    doc.add_page_break()

def assignment(doc):
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('ЗАДАНИЕ\nна курсовой проект'); set_run(r,16,True)
    doc.add_paragraph('Студент: Курбанов Тимур Магомедович').runs[0].font.size=Pt(14)
    doc.add_paragraph('Дисциплина: «Программная инженерия»').runs[0].font.size=Pt(14)
    doc.add_paragraph('Тема: «Разработка веб-приложения по продаже программного обеспечения»').runs[0].font.size=Pt(14)
    add_para(doc,'Цель: закрепить и углубить знания по проектированию, разработке, тестированию и документированию программного обеспечения на основе PCMEF.',first=False)
    add_para(doc,'Задачи: выполнить анализ предметной области; разработать требования и модель данных; реализовать web-интерфейс и REST API; обеспечить аутентификацию, авторизацию, тестирование, развёртывание и документацию.',first=False)
    add_para(doc,'Исходные данные: методические указания, существующий исходный код SoftStore, результаты Maven, JaCoCo и Docker-проверок.',first=False)
    doc.add_paragraph('\nРуководитель проекта: С. В. Свистунов\n\nДата выдачи задания: ____________________\n\nДата защиты: ____________________').paragraph_format.line_spacing=1.5
    doc.add_page_break()

def toc(doc, headings):
    add_heading(doc,'СОДЕРЖАНИЕ',1)
    for h,level in headings:
        p=doc.add_paragraph(); p.paragraph_format.left_indent=Cm(0.6*(level-1)); p.paragraph_format.space_after=Pt(2)
        r=p.add_run(h); set_run(r,12,bold=(level==1))
    doc.add_page_break()

def parse_table(block):
    lines=[x.strip() for x in block.strip().splitlines() if x.strip()]
    rows=[]
    for line in lines:
        if not line.startswith('|') or re.match(r'^\|[- :|]+\|$',line): continue
        rows.append([c.strip() for c in line.strip('|').split('|')])
    return rows

def insert_code(doc, rel, caption):
    cap,txt=code_file(rel,caption); p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; r=p.add_run('Листинг '+cap); set_run(r,11,italic=True)
    for line in txt.splitlines():
        p=doc.add_paragraph(); p.paragraph_format.space_after=Pt(0); p.paragraph_format.line_spacing=1.0
        r=p.add_run(line); set_run(r,8,font_name='Consolas')
    doc.add_paragraph()

def render_report(doc, text, image_map):
    blocks=re.split(r'\n\s*\n',text)
    for block in blocks:
        block=block.strip()
        if not block: continue
        if block.startswith('{{GLOSSARY}}'): add_table(doc,glossary()); continue
        if block.startswith('{{METHODS}}'): add_table(doc,methods()); continue
        if block.startswith('{{TESTS}}'): add_table(doc,tests_table()); continue
        if block.startswith('{{MATRIX}}'): add_table(doc,matrix()); continue
        m=re.match(r'\{\{CODE:([^|]+)\|(.+)\}\}',block)
        if m: insert_code(doc,m.group(1),m.group(2)); continue
        im=re.match(r'!\[([^\]]+)\]\(([^)]+)\)',block)
        if im:
            key=im.group(2); add_image(doc,image_map[key],im.group(1)); continue
        if block.startswith('|'):
            add_table(doc,parse_table(block)); continue
        lines=block.splitlines()
        first=lines[0]
        if first.startswith('# '): add_heading(doc,first[2:],1); continue
        if first.startswith('## '): add_heading(doc,first[3:],2); continue
        if first.startswith('### '): add_heading(doc,first[4:],3); continue
        if first.startswith('- '):
            for line in lines:
                p=doc.add_paragraph(style='List Bullet'); p.paragraph_format.line_spacing=1.5; r=p.add_run(line[2:]); set_run(r)
            continue
        p=doc.add_paragraph(); p.paragraph_format.first_line_indent=Cm(1.25); p.paragraph_format.line_spacing=1.5; p.paragraph_format.space_after=Pt(3)
        for line in lines:
            if not line.strip(): continue
            r=p.add_run(line+' '); set_run(r)

def main():
    # diagrams
    imgs={}
    imgs['idef0.png']=diagram('idef0.png','Контекстная модель продажи программного обеспечения',flow=[('Вход','Сведения о продуктах\nВыбор покупателя','#4f81bd'),('A-0 SoftStore','Продажа ПО через веб-приложение','#1f4e79'),('Выход','Заказ\nDemo-оплата\nЛицензии','#4f81bd')])
    imgs['buc.png']=tree_diagram('buc.png','Бизнес-прецеденты','Продажа программного обеспечения',[('Посетитель',['Просмотр каталога','Поиск продукта','Регистрация и вход']),('Покупатель',['Корзина','Оформление заказа','Оплата и лицензии']),('Администратор',['Категории','Продукты','Контроль заказов'])])
    imgs['usecase.png']=tree_diagram('usecase.png','Варианты использования SoftStore','SoftStore',[('Посетитель',['Каталог','Поиск','Регистрация']),('USER',['Корзина','Заказы','Лицензии']),('ADMIN',['CRUD каталога','Архивирование','Заказы'])])
    imgs['domain.png']=tree_diagram('domain.png','Доменная модель','User',[('Каталог',['Category','SoftwareProduct']),('Покупка',['Cart','CartItem','Order','OrderItem']),('Результат',['Payment','License'])])
    imgs['packages.png']=diagram('packages.png','Фактические зависимости PCMEF',packages=[('Presentation',['Controllers','DTO'],'#4f81bd'),('Control',['IUserService','IOrderService'],'#1f4e79'),('Mediator',['CartMediator','CheckoutFacade'],'#4f81bd'),('Entity',['Order','Cart','License'],'#4f81bd'),('Foundation',['Repositories','JPA'],'#1f4e79')])
    imgs['er.png']=diagram('er.png','Реляционная схема PostgreSQL',packages=[('Каталог',['categories','software_products'],'#4f81bd'),('Пользователь',['users','carts','cart_items'],'#1f4e79'),('Покупка',['orders','order_items','payments','licenses'],'#4f81bd')])
    imgs['cart-sequence.png']=diagram('cart-sequence.png','Добавление продукта в корзину',flow=[('USER','POST cart/items','#4f81bd'),('Controller','ICartService','#1f4e79'),('CartMediator','Проверка и расчёт','#4f81bd'),('Repositories','CartResponse','#1f4e79')])
    imgs['order-sequence.png']=diagram('order-sequence.png','Создание заказа из корзины',flow=[('USER','POST orders','#4f81bd'),('Controller','IOrderService','#1f4e79'),('CheckoutFacade','Проверка и snapshot','#4f81bd'),('OrderRepository','PENDING_PAYMENT','#1f4e79')])
    imgs['payment-sequence.png']=diagram('payment-sequence.png','Оплата и лицензии',flow=[('USER','POST pay','#4f81bd'),('CheckoutFacade','Проверка статуса','#1f4e79'),('PaymentMediator','SUCCESS','#4f81bd'),('LicenseMediator','Ключи и COMPLETED','#1f4e79')])
    imgs['design.png']=diagram('design.png','Ключевые классы оформления заказа',packages=[('Presentation',['OrderV1Controller'],'#4f81bd'),('Control',['IOrderService','IPaymentService'],'#1f4e79'),('Mediator',['CheckoutFacade','PaymentMediator','LicenseMediator'],'#4f81bd'),('Foundation',['OrderRepository','PaymentRepository'],'#1f4e79')])
    imgs['deploy.png']=diagram('deploy.png','Развёртывание SoftStore',flow=[('Браузер','HTML CSS JavaScript','#4f81bd'),('app:8080','Executable WAR','#1f4e79'),('db:5432','PostgreSQL 16','#4f81bd')])
    imgs['gantt.png']=diagram('gantt.png','Учебный план проекта',packages=[('Недели 1–4',['Анализ','Требования'],'#4f81bd'),('Недели 5–10',['Архитектура','БД','API'],'#1f4e79'),('Недели 11–14',['Backend','UI','Security'],'#4f81bd'),('Недели 15–18',['Тесты','Deployment','Docs'],'#1f4e79')])
    imgs['catalog.png']=ROOT/'tmp/course-note/catalog.png'; imgs['auth.png']=ROOT/'tmp/course-note/auth.png'
    doc=Document(); configure_section(doc.sections[0]); setup_styles(doc)
    # footer for body defaults after cover pages
    cover(doc); assignment(doc)
    headings=[]
    for line in REPORT.read_text(encoding='utf-8').splitlines():
        if line.startswith('# '): headings.append((line[2:],1))
        elif line.startswith('## '): headings.append((line[3:],2))
    toc(doc,headings)
    sec=doc.sections[-1]; footer=sec.footer.paragraphs[0]; footer.alignment=WD_ALIGN_PARAGRAPH.CENTER; add_page_field(footer)
    render_report(doc,REPORT.read_text(encoding='utf-8'),imgs)
    for sec in doc.sections:
        configure_section(sec)
        footer=sec.footer.paragraphs[0]; footer.alignment=WD_ALIGN_PARAGRAPH.CENTER
        if not footer.text: add_page_field(footer)
    out=OUT/'Пояснительная записка SoftStore Курбанов Тимур Магомедович.docx'
    doc.save(out); print(out)

if __name__=='__main__': main()
