import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.nio.charset.StandardCharsets;
public class ClientApp {
    private Map<String,Menu> menus;
    private Client client;
    private boolean AUTHENTICATED;
    private Scanner in;
    
    public ClientApp(boolean singleThread) {
        try {
            if (singleThread) this.client = new ClientSingleThread();
            else this.client = new ClientMultiThread();
        }
        catch (IOException e) {
            e.printStackTrace();
            Menu.pressEnterToContinue();
        }
        this.AUTHENTICATED = false;
        this.menus = new HashMap<>();
        this.in = new Scanner(System.in);
    }
    private void menuInicial() {
        Menu menuInicial = menus.get("inicial");
        if (menuInicial == null) {
            String[] menuInicialOps = new String[] {"Autenticar", "Registar"};
            menuInicial = new Menu("Menu Inicial", menuInicialOps);
            menuInicial.setHandler(1, () -> menuAutenticar());
            menuInicial.setHandler(2, () -> menuRegistar());
            this.menus.put("inicial", menuInicial);
        }
        menuInicial.run();
    }
    private void menuAutenticar() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Autenticar --- ");
        System.out.print("Username : ");
        String userName = in.nextLine();
        System.out.print("Password : ");
        String password = in.nextLine();
        AUTHENTICATED = client.authenticate(userName, password);
        if (AUTHENTICATED) {
            String[] menuInicialOps = new String[] {"Get", "Put", "MultiGet", "MultiPut", "GetWhen"};
            Menu menuInicial = new Menu("Menu Inicial - Autenticado", menuInicialOps);
            menuInicial.setHandler(1, () -> menuGet());
            menuInicial.setHandler(2, () -> menuPut());
            menuInicial.setHandler(3, () -> menuMultiGet());
            menuInicial.setHandler(4, () -> menuMultiPut());
            menuInicial.setHandler(5, () -> menuGetWhen());
            this.menus.put("inicial", menuInicial);
            System.out.println("Autenticado com sucesso");
            Menu.pressEnterToContinue();
            menuInicial();
            client.logout();
        }
        else {
            System.out.println("Autenticacao falhou");
            Menu.pressEnterToContinue();
        }
    }
    private void menuRegistar() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Registar --- ");
        System.out.print("Username : ");
        String userName = in.nextLine();
        System.out.print("Password : ");
        String password = in.nextLine();
        boolean registed = client.register(userName, password);
        if (registed) {
            System.out.println("Registado com sucesso");
            Menu.pressEnterToContinue();
        }
        else {
            System.out.println("Registo Falhou");
            Menu.pressEnterToContinue();
        }
    }
    private void menuGet() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Get --- ");
        System.out.print("Key : ");
        String key = in.nextLine();
        byte[] value = client.get(key);
        // Falta alterar para mostrar o value, para ficheiro ou no ecrã
        if (value != null) { //Get efetuado com sucesso
            String s = String.format("Value : %s",new String(value,StandardCharsets.UTF_8));
            System.out.println(s);
            Menu.pressEnterToContinue();
        }
        else { //Get falhou
            String s = String.format("Esta key nao tem um value associado");
            System.out.println(s);
            Menu.pressEnterToContinue();
        }
    }
    private void menuPut() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Put --- ");
        System.out.print("Key : ");
        String key = in.nextLine();
        System.out.print("Value : ");
        // Talvez alterar para ter a opção de enviar um ficheiro
        byte[] value = in.nextLine().getBytes();
        client.put(key, value);
        System.out.println("Put efetuado com sucesso");
        Menu.pressEnterToContinue();
    }
    private void menuMultiGet() {
        int numberOfPairs = -1;
        do {
            Menu.clearTerminal();
            System.out.println("\n --- Menu MultiGet --- ");
            System.out.print("Numero de pares : ");
            String numPairs = in.nextLine();
            try {
                numberOfPairs = Integer.parseInt(numPairs);
            }
            catch (NumberFormatException e) {
                System.out.println("Numero de pares invalido");
                numberOfPairs = -1;
                Menu.pressEnterToContinue();
            }
        }
        while (numberOfPairs == -1);
        Set<String> keys = new LinkedHashSet<>();
        Map<String,byte[]> pairs = new HashMap<>(); // Depois retirar, só para poder testar
        for (int i = 0; i < numberOfPairs; i++) {
            boolean exists = true;
            do {
                System.out.print((i+1) + ".ª key : ");
                String key = in.nextLine();
                exists = keys.contains(key);
                if (exists) {
                    System.out.println("Esta key ja foi introduzida");
                    System.out.println("Introduza uma nova key");
                    Menu.pressEnterToContinue();
                }
                else {
                    keys.add(key);
                    pairs.put(key,new byte[10]); // Depois retirar, só para poder testar
                }
            }
            while (exists);
        }
        Map<String,byte[]> pairs2 = client.multiGet(keys);
        int i = 0;
        for (String k : keys) {
            byte[] v = pairs2.get(k);
            if (v != null) {
                String s = String.format("%d.º value : %s",(i+1),new String(v,StandardCharsets.UTF_8));
                System.out.println(s);
            }
            else {
                String s = String.format("%d.ª key nao tem um value associado",(i+1));
                System.out.println(s);
            }
            i++;
        }
        Menu.pressEnterToContinue();
    }
    private void menuMultiPut() {
        int numberOfPairs = -1;
        do {
            Menu.clearTerminal();
            System.out.println("\n --- Menu MultiPut --- ");
            System.out.print("Numero de pares : ");
            String numPairs = in.nextLine();
            try {
                numberOfPairs = Integer.parseInt(numPairs);
            }
            catch (NumberFormatException e) {
                System.out.println("Numero de pares invalido");
                numberOfPairs = -1;
                Menu.pressEnterToContinue();
            }
        }
        while (numberOfPairs == -1);
        Map<String,byte[]> pairs = new HashMap<>();
        for (int i = 0; i < numberOfPairs; i++) {
            boolean exists = true;
            do {
                System.out.print((i+1) + ".ª key : ");
                String key = in.nextLine();
                exists = (pairs.get(key) != null);
                if (exists) {
                    System.out.println("Esta key ja foi introduzida");
                    System.out.println("Introduza uma nova key");
                    Menu.pressEnterToContinue();
                }   
                else {
                    System.out.print((i+1) + ".º value : ");
                    byte[] value = in.nextLine().getBytes();
                    pairs.put(key,value);
                }
            } while(exists);
        }
        client.multiPut(pairs);
        System.out.println("MultiPut efetuado com sucesso");
        Menu.pressEnterToContinue();
    }
    private void menuGetWhen() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu GetWhen --- ");
        System.out.print("Key : ");
        String key = in.nextLine();
        System.out.print("Key condition : ");
        String keyCond = in.nextLine();
        System.out.print("Value condition : ");
        byte[] valueCond = in.nextLine().getBytes();
        byte[] value = client.getWhen(key,keyCond,valueCond);
        // Falta alterar para mostrar o value, para ficheiro ou no ecra
        if (value != null) {
            String s = String.format("Value : %s",new String(value,StandardCharsets.UTF_8));
            System.out.println(s);
            Menu.pressEnterToContinue();
        }
        else {
            String s = String.format("Esta key nao tem um value associado");
            System.out.println(s);
            Menu.pressEnterToContinue();
        }
    }
    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            boolean singleThread = true;
            if (arg.equals("-s")) {
                singleThread = true;
                ClientApp clientApp = new ClientApp(singleThread);
                clientApp.menuInicial();
            }
            else if (arg.equals("-m")) {
                singleThread = false;
                ClientApp clientApp = new ClientApp(singleThread);
                clientApp.menuInicial();
            }
            else {
                System.out.println("Argumento invalido");
                System.out.println("  -s : para cliente single thread");
                System.out.println("  -m : para cliente multi thread");
            }
        }
        else {
            System.out.println("Numero de argumentos invalido");
            System.out.println("  java ClientApp <argumento>");
            System.out.println("    -s : para cliente single thread");
            System.out.println("    -m : para cliente multi thread");
        }
    }
}
