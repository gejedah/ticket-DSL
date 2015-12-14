import groovy.xml.MarkupBuilder
/**
 * Processes a simple DSL to create various formats of a ticket: html
 */
class TicketDsl {
    private static HashMap<String, Boolean> invocation_effect = new HashMap()
    private static HashMap<String, List<Attr>> obligeSections = new HashMap<>()  // utk menampung optional attribut dari user
    private static HashMap<String, List<Attr>> optSections = new HashMap<>()
    private static Vector<String> obligeAttrs = new Vector<>()
    private static Vector<String> optAttrs = new Vector<>()
    private static String note;

    public TicketDsl(){
        File file_attr = new File("resource/config.txt")
        List<String> oblig_atts = file_attr.readLines()
        for (String att : oblig_atts){
//            println att
            if (att.equalsIgnoreCase("Notice")){
                invocation_effect.put(att, false)
            }
            else{
                invocation_effect.put(att, true)
            }
            obligeAttrs.add(att)
        }

        File file_section = new File("resource/config1.txt")
        List<String> tempList = file_section.readLines()
        for (String section: tempList){
            obligeSections.put(section, new ArrayList<Attr>())
        }
        optSections.put("etc", new ArrayList<Attr>())

        println "Daftar atribut wajib yang ada dari program: "
        int nomor = 0

        for (String att: obligeAttrs){
            println "${nomor+1}." + att
            nomor++
        }

        println()
        println "Daftar bagian yang sudah didefinisikan aplikasi"
        nomor = 0

        for (String section: obligeSections.keySet()){
            println "${nomor+1}." + section
            nomor++
        }

        println "Masukkan input anda: "

    }

    def Notice(String note){
        invocation_effect.replace("Notice", true)
        this.note = note
    }

    /**
     * When a method is not recognized, assume it is a title for a new section. Create a simple
     * object that contains the method name and the parameter which is the body.
     */
    def methodMissing(String methodName, args) {
//        println("Method Missing being invoked")
        boolean member_of_obligeAttrs = false
        boolean member_of_optAttrs = false
        boolean member_of_obligeSects = false
        boolean member_of_optSects = false
        int index_for_changed

        // cek apakah attr sudah ada di attribut wajib
        for (String att: obligeAttrs){
            if (att.equalsIgnoreCase(methodName)){
                member_of_obligeAttrs = true
                index_for_changed = obligeAttrs.indexOf(att)
            }
        }

        // cek apakah attr sudah ada di attribut opsional
        for (String att: optAttrs){
            if (att.equalsIgnoreCase(methodName)){
                member_of_optAttrs = true
            }
        }

        if (member_of_obligeAttrs){
            if (args.length >= 1){
                obligeAttrs.insertElementAt(args[0], index_for_changed)
                obligeAttrs.remove(index_for_changed + 1)
            }
        }
        else if (!member_of_optAttrs){
            optAttrs.add(methodName)
            def Attr = new Attr(name: methodName, vals: args)

            if (args.length >= 1){

                for (String section: obligeSections.keySet()){
                    if (section.equalsIgnoreCase(args[0])){
                        member_of_obligeSects = true
                        args[0] = section
                    }
                }

                if (member_of_obligeSects){
                    obligeSections.get(args[0]).add(Attr)
//                println "isi dari sect: " + obligeSections.get(args[0])
                }
                else{

                    for (String section: optSections.keySet()){
                        if (section.equalsIgnoreCase(args[0])){
                            member_of_optSects = true
                            args[0] = section
                        }
                    }

                    if (member_of_optSects){
                        optSections.get(args[0]).add(Attr)
                    }
                    else {
                        ArrayList<Attr> temp = new ArrayList<>()
                        temp.add(Attr)
                        optSections.put(args[0], temp)
                    }
//                println "isi dari sect: " + optSections.get(args[0])
                }
            }
            else{
                optSections.get("etc").add(Attr)
//            println "isi dari sect: " + obligeSections.get("etc")
            }
        }
    }

    /**
     * 'get' methods get called from the dsl by convention. Due to groovy closure delegation,
     * we had to place MarkUpBuilder and StringWrite code in a static method as the delegate of the closure
     * did not have access to the system.out
     */
    def html() {
        doHtml(this)
    }

    /**
     * Use markupBuilder to create an html output
     */
    private static doHtml(TicketDsl ticketDsl) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        if (invocation_effect.containsValue(false)){
            println("Nilai atribut notice belum didefinisikan!!")
        }
        else{
            File file = new File("src/ticket_template.html")
            xml.html() {
                head {
                    title("Ticket")
                    style('''body{
                        background:none;
                    }

                    .container {
                        width:960px;
                        margin:auto;
                    }

                    .logo, .logo img {
                        width:200px;
                    }
                    .logo{
                        margin:auto;
                    }
                    .brand {
                        text-align: center;
                        color:red;
                        margin:0;
                    }
                    .tagline {
                        text-align:center;
                        margin:0;
                    }
                    .clear{clear:both;}
                            .section-required, .booking-details-label, .passenger-details-label, .itinerary-details-label, .payment-details-label, .notice-label{border-bottom: 1px solid;}
                            #passenger-details-table-header, #itinerary-details-table-header{text-align:left;}
                            .payment-table-right-column{text-align: right;}''')
                }
                body {
                    int idx = 0
                    ArrayList<String> att_per_section
                    div(class: "container") {
                        div(class: "logo") {
                            img(src: "logo.png", alt: "Logo", align: "middle")
                            h1(class: "brand required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$".concat(obligeAttrs.get(idx).replace(" ","_")))
                            idx++
                            p(class: "tagline required", id: "${obligeAttrs.get(idx).replace(" ","_")}") {
                                strong("\$".concat(obligeAttrs.get(idx).replace(" ","_")))
                                idx++
                            }
                        }
                        div(class: "clear")
                        div(class: "booking details") {
                            h3(class: "booking-details-label", "Booking Details")
                            table(width: "100%") {
                                tr() {
                                    td(obligeAttrs.get(idx))
                                    td(class: "required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(obligeAttrs.get(idx))
                                    td(class: "required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                }
                                tr() {
                                    td(obligeAttrs.get(idx))
                                    td {
                                        strong(class: "required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$".concat(obligeAttrs.get(idx).replace(" ","_")))
                                        idx++
                                    }
                                }
                            }

                            // Jika ada tambahan input attribut opsional pada section ini
                            att_per_section = obligeSections.get("Booking")
                            if (att_per_section.size() > 0){
                                table(width: "100%") {
                                    for (Attr att : att_per_section) {
                                        tr() {
                                            td(class: "payment-table-left-column", att.name)
                                            td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                        }
                                    }
                                }
                            }
                        }
                        div(class: "passenger-details") {
                            h3(class: "passenger-details-label", "Passenger Details")
                            table(width: "100%") {
                                tr(id: "passenger-details-table-header") {
                                    th(obligeAttrs.get(idx))
                                    th(obligeAttrs.get(idx+1))
                                }
                                writer.append('''\n\t\t#foreach($passenger in $passengers)''')
                                tr() {
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$passenger.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$passenger.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                }
                                writer.append('''\n\t\t#end''')
                            }

                            // Jika ada tambahan input attribut opsional pada section ini
                            att_per_section = obligeSections.get("Passenger")
                            if (att_per_section.size() > 0){
                                table(width: "100%") {
                                    for (Attr att : att_per_section) {
                                        tr() {
                                            td(class: "payment-table-left-column", att.name)
                                            td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                        }
                                    }
                                }
                            }
                        }
                        div(class: "itinerary-details") {
                            h3(class: "itinerary-details-label", "Itenarary Details")
                            table(width: "100%") {
                                tr(id: "itinerary-details-table-header") {
                                    int iter_temp = 0
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    th(obligeAttrs.get(idx + iter_temp))
                                    iter_temp++
                                    if (!obligeAttrs.get(idx+iter_temp).equalsIgnoreCase("\"\"")){
                                        th(obligeAttrs.get(idx + iter_temp))
                                    }
                                }
                                writer.append('''\n\t\t#foreach($itinerary in $itineraries)''')
                                tr() {
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    idx++
                                    if (!obligeAttrs.get(idx).equalsIgnoreCase("\"\"")){
                                        td(class: "looping-required", id: "${obligeAttrs.get(idx).replace(" ","_")}", "\$itinerary.".concat(obligeAttrs.get(idx).replace(" ","_")))
                                    }
                                    idx++
                                }
                                writer.append('''\n\t\t#end''')
                            }

                            // Jika ada tambahan input attribut opsional pada section ini
                            att_per_section = obligeSections.get("Itinerary")
                            if (att_per_section.size() > 0){
                                table(width: "100%") {
                                    for (Attr att : att_per_section) {
                                        tr() {
                                            td(class: "payment-table-left-column", att.name)
                                            td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                        }
                                    }
                                }
                            }
                        }
                        div(class: "payment-details") {
                            h3(class: "payment-details-label", "Payment Details")
                            table(width: "100%") {
                                String s1 = "\$".concat(obligeAttrs.get(idx).replace(" ","_"))
                                tr() {
                                    td(class: "payment-table-left-column", obligeAttrs.get(idx))
                                    td(class: "payment-table-right-column required", id: "${obligeAttrs.get(idx).replace(" ","_")}", s1)
                                    idx++
                                }
                                String s2 = "\$".concat(obligeAttrs.get(idx).replace(" ","_"))
                                tr() {
                                    td(class: "payment-table-left-column", obligeAttrs.get(idx))
                                    td(class: "payment-table-right-column required", id: "${obligeAttrs.get(idx).replace(" ","_")}", s2)
                                    idx++
                                }
                                tr() {
                                    td(class: "payment-table-left-column", "Total")
                                    td(class: "payment-table-right-column", id: "total", '''$math.add('''.concat(s1).concat(",").concat(s2).concat(")"))
                                }
                            }

                            // Jika ada tambahan input attribut opsional pada section ini
                            att_per_section = obligeSections.get("Payment")
                            if (att_per_section.size() > 0){
                                table(width: "100%") {
                                    for (Attr att : att_per_section) {
                                        tr() {
                                            td(class: "payment-table-left-column", att.name)
                                            td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                        }
                                    }
                                }
                            }
                        }
                        div(class: "notice") {
                            h3(class: "notice-label", "Notice")
                            p(id: "${obligeAttrs.get(idx)}", note)

                            // Jika ada tambahan input attribut opsional pada section ini
                            att_per_section = obligeSections.get("Notice")
                            if (att_per_section.size() > 0){
                                table(width: "100%") {
                                    for (Attr att : att_per_section) {
                                        tr() {
                                            td(class: "payment-table-left-column", att.name)
                                            td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                        }
                                    }
                                }
                            }
                        }
                        Set<String> sections = optSections.keySet()
                        for (String section : sections) {
                            att_per_section = optSections.get(section)
                            if (att_per_section.size() > 0){
                                div(class: section) {
                                    h3(class: "section-required", section)
                                    table(width: "100%") {
                                        for (Attr att : att_per_section) {
                                            tr() {
                                                td(class: "payment-table-left-column", att.name)
                                                td(class: "payment-table-right-column required", id: att.name.replace(" ","_"), "\$".concat(att.name).replace(" ","_"))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            file.write(writer.toString())
            println "Template being generated!!"
        }
    }

    public static void main(String[] args){
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(System.in));

        TicketDsl ticketDsl = new TicketDsl()
        String input;
        String[] inputs
        String[] param
        ArrayList tes = new ArrayList<>()

        while ((input = bfReader.readLine()) != null && input.length()!= 0 && input != "exit") {
            inputs = input.split("[<>]")
            if (input.length() > 1){
                param = Arrays.copyOfRange(inputs, 1, inputs.length)
//                println "byk elemen yg diinput: " + inputs.length
//                for (int i = 0; i < inputs.length; i++) {
//                    println "Elemen ke $i adalah " + inputs[i]
//                }
            }
            tes = param
            tes.removeAll(Collections.singleton("")) // remove ""
            ticketDsl.invokeMethod(inputs[0], param)
        }
        println "Finished "

    }
}

