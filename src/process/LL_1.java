package process;

import entity.Grammar;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LL_1 {
    public static String testStr = "S -> D b B\n" +
            "D -> d | ε\n" +
            "B -> a | B b a | ε";
//    public static String testStr = "E -> E + T | T\n"+
//              "T -> T * F | F\n"+
//              "F -> ( E ) | id";
//    public static String testStr = "S -> ( S ) S | ε";
//    public static String testStr = "S -> a A d | a B e\n"+
//              "A -> c\n"+
//              "B -> b";
    public static Set<String> terminator = new HashSet<>();
    public static Set<String> nonTerminal = new HashSet<>();
    public static String start;

    public static HashMap<String,Grammar> grammars = new HashMap<>();
    //初始化文法
    public static void init(String input)
    {
        String[] strings = input.split("\n");
        boolean isStart = true;
        for (String str:strings){
            String[] lr = str.split(" -> ");
            if(isStart)
            {
                start=lr[0];
                isStart=false;
            }
            String[] right = lr[1].split(" \\| ");
            ArrayList<ArrayList<String>> content = new ArrayList<>();
            for (String r:right)
            {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.addAll(Arrays.asList(r.split(" ")));
                content.add(arrayList);
                terminator.addAll(arrayList);
            }
            nonTerminal.add(lr[0]);
            if(terminator.contains(lr[0]))
            {
                terminator.remove(lr[0]);
            }
            Grammar grammar = new Grammar(lr[0],lr[1],content);
            grammars.put(lr[0],grammar);
        }

    }
    //消除左递归
    public static void eliminateRecursion()
    {
        //遍历文法
        HashMap<String,Grammar> temporary = new HashMap<>();//暂存新生成的文法
        grammars.forEach((k,v)->{
            //遍历content
            for(ArrayList<String> array:v.getContent())
            {
                if(k.equals(array.get(0))){
                    //添加A'文法
                    //A'->αA'|ε
                    ArrayList<ArrayList<String>> new_content = new ArrayList<>();
                    ArrayList<String> epsilon = new ArrayList<>();
                    epsilon.add("ε");
                    array.remove(0);
                    String new_left = k + '\'';
                    array.add(new_left);
                    new_content.add(array);
                    new_content.add(epsilon);
                    StringBuilder new_right = new StringBuilder();
                    for (String str:array)
                    {
                        new_right.append(str).append(" ");
                    }
                    new_right.append("| ε");
//                    System.out.println(new_content);
                    nonTerminal.add(new_left);
                    terminator.add("ε");
                    temporary.put(new_left,new Grammar(new_left,new_right.toString(),new_content));
                    //改写当前文法
                    //A->βA'
                    v.getContent().remove(array);
                    StringBuilder right = new StringBuilder();
                    for(ArrayList<String> newArray:v.getContent())
                    {
                        newArray.add(new_left);
                        for(String str : newArray)
                        {
                            right.append(str+" ");
                        }
                        right.append("| ");
                    }
                    v.setRight(right.substring(0,right.length()-2).toString().trim());
                }
            }
        });
        grammars.putAll(temporary);
    }
    //提取左因子
    public static void extractFactor()
    {
        HashMap<String,Grammar> temporary = new HashMap<>();//暂存新生成的文法
        grammars.forEach((k,v)->{
            StringBuilder common = new StringBuilder();
            ArrayList<String> factor = new ArrayList<>();
            int min = v.getContent().get(0).size();
            for(ArrayList<String> array:v.getContent())
            {
                if(min>array.size())
                    min=array.size();
            }
            boolean flag = true;
            int t=0;
            if(v.getContent().size()!=1)
            while(flag){
                for(int i=0;i<v.getContent().size()-1;i++)
                {
                    if(!v.getContent().get(i).get(t).equals(v.getContent().get(i+1).get(t))||t>min-1)
                    {
                        flag=false;
                        break;
                    }
                }
                if (flag) {
                    factor.add(v.getContent().get(0).get(t));
                    common.append(v.getContent().get(0).get(t++)).append(" ");
                }

            }
            //添加A'文法
            //A'->β|γ
            if(!common.toString().equals("")) {
                ArrayList<ArrayList<String>> new_content = new ArrayList<>();
                for (int i = 0; i < v.getContent().size(); i++) {
                    v.getContent().get(i).removeAll(factor);
                    new_content.add(v.getContent().get(i));
                }
                String new_left = k + '\'';
                StringBuilder new_right = new StringBuilder();
                for (ArrayList<String> c : new_content) {
                    for (String str : c) {
                        new_right.append(str).append(" ");
                    }
                    new_right.append("| ");
                }
                nonTerminal.add(new_left);
                temporary.put(new_left, new Grammar(new_left, new_right.substring(0,new_right.length()-2).toString().trim(), new_content));
//            //改写当前文法
//            //A->αA'
                ArrayList<ArrayList<String>> now_content = new ArrayList<>();
                factor.add(new_left);
                now_content.add(factor);
                StringBuilder right = new StringBuilder();
                for (ArrayList<String> c : now_content) {
                    for (String str : c) {
                        right.append(str).append(" ");
                    }
                }
                v.setContent(now_content);
                v.setRight(right.toString().trim());
            }
        });
        grammars.putAll(temporary);
    }
    //求First集
    public static void findFirst(Grammar grammar,HashSet<String> first){
        for(ArrayList<String> str:grammar.getContent()){

            String suspected = str.get(0);
            //终结符
            if(terminator.contains(suspected)){
                if(suspected.equals("ε")&&str.size()>1){
                    for(int i=1;i<str.size();i++)
                    {
                        if(!str.get(i).equals("ε")) {
                            if(terminator.contains(str.get(i))){
                                first.add(str.get(i));
                                break;
                            }
                            else {
                                HashSet<String> set = new HashSet<>();
                                findFirst(grammars.get(str.get(i)), set);
                                first.addAll(set);
                                if(!set.contains("ε"))
                                    break;
                            }

                        }
                    }
                }
                else
                first.add(suspected);
            }
            else
            //非终结符
            {
                for(int i = 0;i<str.size();i++) {
                    if(terminator.contains(str.get(i))){
                        first.add(str.get(i));
                        first.remove("ε");
                        break;
                    }
                    HashSet<String> set = new HashSet<>();
                    findFirst(grammars.get(str.get(i)),set);
                    first.addAll(set);
                    if(!set.contains("ε"))
                        break;
                }


            }
        }
    }
    public static void getFirst(){
        System.out.println("First集合");
        grammars.forEach((k,v)-> {
            HashSet<String> first = new HashSet<>();
            findFirst(v,first);
            System.out.println(k+" :"+first);
            v.setFirst(first);
        });
    }
    //求Follow集合
    public static void findFollow(String key) {
        grammars.forEach((k,v)->{
            for(ArrayList<String> content: v.getContent()){
                for(int i=0;i<content.size();i++){
                    String str = content.get(i);
                    if(str.equals(key)){
                        if(i==content.size()-1){
                            grammars.get(str).getFollow().addAll(grammars.get(k).getFollow());
                        }
                        else
                        {
                            if(nonTerminal.contains(content.get(i+1)))
                            {
                                grammars.get(str).getFollow().addAll(grammars.get(content.get(i+1)).getFirst());
                                if(grammars.get(str).getFollow().contains("ε"))
                                {
                                    grammars.get(str).getFollow().remove("ε");
                                    grammars.get(str).getFollow().addAll(grammars.get(k).getFollow());
                                }
                            }
                            else{
                                grammars.get(str).getFollow().add(content.get(i+1));
                            }
                        }
                    }
                }
            }
        });
    }
    public static void getFollow(){
        grammars.get(start).getFollow().add("$");
        grammars.forEach((k,v)->{
            for(ArrayList<String> content: v.getContent()){
                for(int i=0;i<content.size();i++){
                    String str = content.get(i);

                    if(nonTerminal.contains(str)){
                        if(i==content.size()-1){
                            if(grammars.get(k).getFollow().isEmpty())
                                findFollow(k);
                            grammars.get(str).getFollow().addAll(grammars.get(k).getFollow());
                        }
                        else
                        {
                            if(nonTerminal.contains(content.get(i+1)))
                            {
                                grammars.get(str).getFollow().addAll(grammars.get(content.get(i+1)).getFirst());
                                if(grammars.get(str).getFollow().contains("ε"))
                                {
                                    grammars.get(str).getFollow().remove("ε");
                                    if(grammars.get(k).getFollow().isEmpty())
                                        findFollow(k);
                                    grammars.get(str).getFollow().addAll(grammars.get(k).getFollow());
                                }
                            }
                            else{
                                grammars.get(str).getFollow().add(content.get(i+1));
                            }
                        }
                    }
                }
            }
        });
    }
    static HashMap<Integer,HashSet<String>> select = new HashMap<>();
    //求SELECT集合
    public static void getSelect(){
        AtomicInteger id= new AtomicInteger();
        grammars.forEach((k,v)->{

            for(ArrayList<String> content:v.getContent()){
                HashSet<String> set = new HashSet<>();
                if(nonTerminal.contains(content.get(0)))//是非终结符
                {
                    boolean isBreak = false;
                    for(int i=0;i<content.size();i++)
                    {
                        if(nonTerminal.contains(content.get(i))){//是非终结符
                            set.addAll(grammars.get(content.get(i)).getFirst());
                            if(!set.contains("ε")){
                                isBreak=true;
                                break;
                            }
                            else
                            {
                                set.remove("ε");
                            }
                        }
                        else
                        {
                            if(!content.get(i).equals("ε")){
                                set.add(content.get(i));
                                isBreak=true;
                                break;
                            }
                        }
                    }
                    if(!isBreak)
                        set.addAll(v.getFollow());
                }
                else {
                    if(content.get(0).equals("ε")){
                        boolean isBreak = false;
                        for(int i=1;i<content.size();i++)
                        {
                            if(nonTerminal.contains(content.get(i))){//是非终结符
                                set.addAll(grammars.get(content.get(i)).getFirst());
                                if(!set.contains("ε")){
                                    isBreak=true;
                                    break;
                                }
                                else
                                {
                                    set.remove("ε");
                                }
                            }
                            else
                            {
                                if(!content.get(i).equals("ε")){
                                    set.add(content.get(i));
                                    isBreak=true;
                                    break;
                                }
                            }
                        }
                        if(!isBreak)
                            set.addAll(v.getFollow());
                    }
                    else
                        set.add(content.get(0));
                }
                select.put(id.getAndIncrement(),set);
            }

        });
    }
    static HashMap<String,ArrayList<HashMap<String,String>>> table = new HashMap<>();
    //获取分析表
    public static void getAnalysis(){
        AtomicInteger id= new AtomicInteger();
        grammars.forEach((k,v)->{
            ArrayList<HashMap<String,String>> outer = new ArrayList<>();
            for(ArrayList<String> content:v.getContent())
            {
                StringBuilder exp = new StringBuilder(k + " -> ");
                for (String s : content) exp.append(s).append(" ");

                for(String str:select.get(id.getAndIncrement())){
                    HashMap<String, String> inner = new HashMap<>();
                    inner.put(str,exp.toString());
                    outer.add(inner);
                }
            }
            table.put(k,outer);
        });
    }
    public static void showTable(){
        myFrame myFrame=new myFrame();
    }
    //分析
    public static void analysis(String str){

        Stack<String> ana = new Stack<>();
        Stack<String> exp = new Stack<>();
        String[] spl = str.split(" ");

        exp.push("$");
        exp.addAll(Arrays.asList(spl));
        ana.push("$");
        ana.push(start);
        System.out.printf("%25s%25s%25s\n","分析栈","输入","动作");
        while(!ana.isEmpty()){
            boolean isOK = false;
            String result ="";
            Stack<String> convert = (Stack<String>) ana.clone();
            StringBuilder s1 = new StringBuilder();

            while(!convert.isEmpty()){
                s1.append(convert.pop());
            }
            convert = (Stack<String>) exp.clone();
            StringBuilder s2 = new StringBuilder();
            while(!convert.isEmpty()){
                s2.append(convert.pop());
            }
            System.out.printf("%30s%30s", s1.toString(),s2.toString());
            if(nonTerminal.contains(ana.peek()))
            {
                for(HashMap<String,String> map:table.get(ana.peek())){
                    if(map.containsKey(exp.peek())){
                        result = map.get(exp.peek());
                        String []lr = result.split(" -> ");
                        String []right = lr[1].split(" ");
                        ana.pop();
                        for (int i=right.length-1;i>=0;i--)
                            if(!right[i].equals("ε"))
                                ana.push(right[i]);
                        isOK = true;
                        break;
                    }
                }

            }
            else {
                if(ana.peek().equals(exp.peek()))
                {
                    result="匹配";
                    if(ana.peek().equals("$"))
                        result="接受";
                    ana.pop();
                    exp.pop();
                    isOK=true;
                }
            }
            if(!isOK){
                result="err";

            }
            System.out.printf("%30s",result);
            System.out.println("");
            if (!isOK)
                break;
        }

    }
    public static void main(String[] args) {
        System.out.println("请输入文法(END表示结束)");
        StringBuilder in =new StringBuilder();
        Scanner input=new Scanner(System.in);
        while(true) {
            String str = input.nextLine();
            if(str.equals("END"))
                break;
            in.append(str).append("\n");
        }
        System.out.println(in.toString().trim());
        init(in.toString().trim());
        System.out.println("初始文法");
        System.out.println(grammars);
        eliminateRecursion();
        System.out.println("消除左递归后文法");
        System.out.println(grammars);
        extractFactor();
        System.out.println("提取左因子后文法");
        System.out.println(grammars);
        System.out.println("改写后文法");
        grammars.forEach((k,v)->{
            System.out.println(v.getLeft()+" -> "+v.getRight());
                });
        System.out.println("终结符");
        System.out.println(terminator);
        System.out.println("非终结符");
        System.out.println(nonTerminal);
        System.out.println("文法起始点");
        System.out.println(start);
        getFirst();
        getFollow();
        System.out.println("Follow集合");
        grammars.forEach((k,v)->{
            System.out.println(k+":"+v.getFollow());
        });
        getSelect();
        System.out.println("Select集合");
        System.out.println(select);
        getAnalysis();
        System.out.println("LL(1)分析表");
        System.out.println(table);
        showTable();
        System.out.println("请输入要分析的内容");
        String toA = input.nextLine();
        StringBuilder toDO = new StringBuilder(toA) ;
        System.out.println("分析过程");
        analysis(toDO.reverse().toString());
    }
}
class myFrame extends JFrame {
    public myFrame(){
        setBounds(100,100,400,200);
        String[] columnNames = new String[LL_1.terminator.size()+1];
        Iterator it1 = LL_1.terminator.iterator();
        columnNames[0]="";
        int id=1;
        while (it1.hasNext()){
            String str = (String) it1.next();
            if(!str.equals("ε")){
                columnNames[id++]=str;
            }
            else
                columnNames[id++]="$";
        }
        String[][] obj = new String[LL_1.nonTerminal.size()+1][LL_1.terminator.size()+1];
        for(int i=1;i<LL_1.terminator.size()+1;i++){
            obj[0][i]=columnNames[i];
        }
        Iterator it2 = LL_1.nonTerminal.iterator();
        id=1;
        while (it2.hasNext()){
            obj[id++][0]=it2.next().toString();
        }
        LL_1.table.forEach((k,v)->{
            int x;
            int y;
            for(int i=1;i<LL_1.nonTerminal.size()+1;i++)
                if(k.equals(obj[i][0])){
                    x=i;
                    for(HashMap<String, String> map:v){
                        for(int j=1;j<LL_1.terminator.size()+1;j++)
                            if(map.containsKey(obj[0][j])){
                                y=j;
                                obj[x][y]=map.get(obj[0][j]);
                                break;
                            }
                    }
                    break;
                }
                });
        JTable analysis = new JTable(obj,columnNames);
        setTitle("分析表");
        analysis.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(analysis);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

}

