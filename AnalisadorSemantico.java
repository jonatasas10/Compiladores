import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class AnalisadorSemantico  {
    public static void main(String [] args) throws Exception{    
               
        String nome = "arquivo.txt";
        
        File file = new File(nome);
        Scanner arquivo = new Scanner(file);
        PrintWriter writer = new PrintWriter("Saida.txt", "UTF-8");
        String linha2, aux;
        linha2 = "";

        while(arquivo.hasNextLine()){
            aux = arquivo.nextLine();
            linha2 = linha2 + aux + "\n";  

                    
        }
        
        // iniciando a leitura do arquivo por linhas
        String linha = linha2.replaceAll("//.*|/\\*((.|\\n)(?!=*/))+\\*/", ""); 
        String espaco = "(\\s+|\\n)";
        String delimitador = "((?<=\\()|(?=\\()|(?<=\\{)|(?=\\{)|(?<=\\[)|(?=\\[)|"; 
        String delimitador2 = "(?<=\\.)|(?=\\.)|(?<=\\))|(?=\\))|(?<=\\+)|(?=\\+)|(?<=\\-)|(?=\\-)|";
        String delimitador3 = "(?<=\\})|(?=\\})|(?<=,)|(?=,)|(?<=;)|(?=;))";
        String delim = delimitador + delimitador2 + delimitador3;
        
        ArrayList <String> comando = new ArrayList<>(Arrays.asList(linha.split(espaco)));
        ArrayList<String> codigo = new ArrayList<>();
        
        
        for (String str : comando){
            ArrayList<String> aux1 = new ArrayList<>(Arrays.asList(str.split(delim)));
            for (int i = 0; i < aux1.size(); i++){
                if (aux1.get(i).matches("\\d+") && aux1.size() > 2 && aux1.get(i+1).equals(".")){
                    String str2 = aux1.get(i) + aux1.get(i+1) + aux1.get(i+2);
                    codigo.add(str2);
                    writer.println(str2);
                    i += 2;
                }
                else if (aux1.get(i).equals("-")){            
                    
                    String str2 = aux1.get(i) + aux1.get(i+1);
                    
                    codigo.add(str2);
                    writer.println(str2);
                    i++;
                }
                else{
                    
                    codigo.add(aux1.get(i));
                    writer.println(aux1.get(i));
                    
                }
            }
            
            
        }   
        
        ArrayList <String> varDecl = new ArrayList<>();
        varDecl = variaveis(codigo); 
        System.out.println(varDecl);                             
        funcao(codigo);
        arquivo.close();
        writer.close();
    }

    public static ArrayList<String> variaveis(ArrayList<String> codigo){
        ArrayList<String> variaveisDecl = new ArrayList<>();
        ArrayList<String> variaveisDeclAntes = null;
        
        for (int i = 0; i < codigo.size(); i++){
            String str = codigo.get(i);
            if (str.equals("int") || str.equals("float")){
                variaveisDecl.add(str);
                variaveisDecl.add(codigo.get(i+1));
            }
            else if (str.equals("String") && codigo.get(i+1).equals("[") && codigo.get(i+2).equals("]")){
                str = str + codigo.get(i+1) + codigo.get(i+2);
                variaveisDecl.add(str);
                variaveisDecl.add(codigo.get(i+3));
            }
            else if (str.equals("=")){
                
                erroVariaveis(variaveisDecl, codigo.get(i-1), codigo.get(i+1));
                
            }
            else if (str.equals("public")){
                variaveisDeclAntes = new ArrayList<>(variaveisDecl);
                variaveisDecl.removeAll(variaveisDecl);
            }
            else if (str.matches("[a-zA-Z][a-zA-Z0-9_]*") && codigo.get(i+1).equals("(") &&
                !codigo.get(i-1).matches("int|new") && !str.matches("main|println")){
                    for (int j = i+1; j < codigo.size(); j++){
                        if (codigo.get(j).matches("[a-zA-Z][a-zA-Z0-9_]*") && !variaveisDecl.contains(codigo.get(j))){
                            System.out.println("Variavel como argumento não declarada: " + codigo.get(j));                            
                        }
                        if (codigo.get(j).equals(")")) break;

                    }
                //System.out.println(str);
            }
            
        }
        //System.out.println(variaveisDeclAntes);
        return variaveisDeclAntes;
    }

    public static void erroVariaveis (ArrayList<String> variaveis, String varNome, String atr){
        
        for (int i = 0; i < variaveis.size(); i++){
            if (varNome.equals(variaveis.get(i))){
                
                if (variaveis.get(i-1).equals("int") && !atr.matches("^[+-]?(\\d+)*") && !atr.matches("[a-zA-Z][a-zA-Z0-9_]*")){
                    System.out.println("Erro de atribuição de inteiro: " + atr);
                }
                else if (variaveis.get(i-1).equals("int") && !variaveis.contains(atr) && !atr.matches("^[+-]?(\\d+).*") ){
                    System.out.println("Erro de atribuição, variável não declarada: " + atr);
                }
            
            }
            else if (!variaveis.contains(varNome)){
                System.out.println("Erro de atribuição, variável não declarada: " + varNome);
                break;
            }
        }
    }

    public static void funcao(ArrayList<String> codigo){
        ArrayList<String> inteiro = new ArrayList<>();
        String nomeFuncao = null;
        int inicio = 0, continua = 0;
        for (int i = 0; i < codigo.size(); i++){
            if (codigo.get(i).equals("public") && codigo.get(i+1).equals("int")){
                nomeFuncao = codigo.get(i+2);
                i += 3;
                inicio = 1;
            }
            else if (!codigo.get(i).equals(",") && inicio == 1){
                inteiro.add(codigo.get(i));

                if (codigo.get(i+1).equals(")")){
                    continua = i+2;
                    break;
                }
            }
        }                
        
        errosParametro(codigo, inteiro, nomeFuncao);

        ArrayList<String> variaveisDecl = new ArrayList<>();

        for (int i = continua; i < codigo.size(); i++){
            String str = codigo.get(i);
            if (str.equals("int") || str.equals("float")){
                variaveisDecl.add(str);
                variaveisDecl.add(codigo.get(i+1));
            }
            if (codigo.get(i).equals("return")){
                if (!variaveisDecl.contains(codigo.get(i+1))){
                    if (!codigo.get(i+1).matches("^[+-]?(\\d+)*")){
                        
                        System.out.println("Erro de atribuição de retorno da função: " + codigo.get(i+1));
                    }
                }
                
            }
        }
        
    }

    public static void errosParametro(ArrayList<String> codigo, ArrayList<String> parametros, String nomeFuncao){
        int inicio = 0;
        ArrayList<String> parametrosInst = new ArrayList<>();

        for (int i = 0; i < codigo.size(); i++){
            if (codigo.get(i).equals(nomeFuncao) && !codigo.get(i-1).equals("int")){
                i += 1;
                inicio = 1;
            }
            else if (inicio == 1 && !codigo.get(i).equals(",")){
                parametrosInst.add(codigo.get(i));
                if (codigo.get(i+1).equals(")")) break;
            }
        }
        //System.out.println(parametros);
        //System.out.println(parametrosInst);

        if (parametros.size()/2 != parametrosInst.size()){
            System.out.println("Quantidade desigual de parâmetros da função " + nomeFuncao);
        }

        for (int i = 0, j = 0; i < parametros.size() && j < parametrosInst.size(); i+=2, j++){
            if (parametros.get(i).equals("int") && (!parametrosInst.get(j).matches("^[+-]?(\\d+)*"))){
                System.out.println("Argumento " + parametrosInst.get(j) + " não é do tipo inteiro");
            }
        }
    }
}
