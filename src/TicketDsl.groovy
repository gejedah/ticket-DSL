import groovy.xml.MarkupBuilder
/**
 * Processes a simple DSL to create various formats of a memo: html
 */
class TicketDsl {

    String toText
    String fromText
    String body
    def sections = []

    private static HashMap<String, Boolean> invocation_effect = new HashMap()

    public TicketDsl(){
        invocation_effect.put("to", false)
        invocation_effect.put("from", false)
        invocation_effect.put("body", false)
    }

    /**
     * This method accepts a closure which is essentially the DSL. Delegate the closure methods to
     * the DSL class so the calls can be processed
     */
    def static make(closure) {
        TicketDsl ticketDsl = new TicketDsl()
        // any method called in closure will be delegated to the ticketDsl class
        closure.delegate = TicketDsl
        closure()
    }

    /**
     * Store the parameter as a variable and use it later to output a memo
     */
    def to(String toText) {
        this.toText = toText
        invocation_effect.replace("to", true)
        println("Method to being invoked")
    }

    def from(String fromText) {
        this.fromText = fromText
        invocation_effect.replace("from", true)
        println("Method from being invoked")
    }

    def body(String bodyText) {
        this.body = bodyText
        invocation_effect.replace("body", true)
        println("Method body being invoked")
    }

    /**
     * When a method is not recognized, assume it is a title for a new section. Create a simple
     * object that contains the method name and the parameter which is the body.
     */
    def methodMissing(String methodName, args) {
        println("Method Missing being invoked")
        def section = new Section(title: methodName, body: args)
        sections << section
    }

    /**
     * 'get' methods get called from the dsl by convention. Due to groovy closure delegation,
     * we had to place MarkUpBuilder and StringWrite code in a static method as the delegate of the closure
     * did not have access to the system.out
     */
    def getHtml() {
        println("Method getHtml being invoked")
        doHtml(this)
    }

    /**
     * Use markupBuilder to create an html output
     */
    private static doHtml(TicketDsl ticketDsl) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        if (invocation_effect.containsValue(false)){
            println("Ada atribut yang belum didefinisikan!!")
        }
        else{
            xml.html() {
                head {
                    title("Memo")
                }
                body {
                    h1("Memo")
                    h3(div: "to", ticketDsl.toText)
                    h3(div: "from", ticketDsl.fromText)
                    p(ticketDsl.body)
                    // cycle through the stored section objects and create uppercase/bold section with body
                    for (s in ticketDsl.sections) {
                        p {
                            b(s.title.toUpperCase())
                        }
                        p(s.body)
                    }
                }
            }
            File file = new File("src/out.html")
            file.write(writer.toString())
//        println writer
        }
    }

    public static void main(String[] args){
        println "Masukkan input anda: "

        BufferedReader bfReader = new BufferedReader(new InputStreamReader(System.in));

        TicketDsl ticketDsl = new TicketDsl()
        String input;
        String[] inputs
        String[] param

        while ((input = bfReader.readLine()) != null && input.length()!= 0 && input != "exit") {
            inputs = input.split("\\s+")
            if (input.length() > 1){
                param = Arrays.copyOfRange(inputs, 1, inputs.length)
//            println "byk elemen yg diinput: " + inputs.length
//            for (int i = 0; i < inputs.length; i++) {
//                println "Elemen ke $i adalah " + inputs[i]
//            }
            }
            ticketDsl.invokeMethod(inputs[0], param)
            println "Masukkan input anda: "
        }

    }
}

