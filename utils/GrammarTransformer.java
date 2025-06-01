package utils;

import parser.Grammar;
import java.util.*;

/**
 * Класс GrammarTransformer нормализует контекстно-свободную грамматику:
 * 1) Удаляет негенерирующие и недостижимые символы
 * 2) Удаляет ε-правила, добавляя все комбинации без nullable-символов
 * В этом файле также выводится результат нормализации.
 */
public class GrammarTransformer {

    public static Grammar transformGrammar(Grammar inputGrammar) {
        // Копируем множества
        Set<String> VN = new HashSet<>(inputGrammar.getNonTerminals());
        Set<String> VT = new HashSet<>(inputGrammar.getTerminals());
        String S = inputGrammar.getStartSymbol();

        // Копируем правила в изменяемую структуру
        Map<String, List<List<String>>> P = new LinkedHashMap<>();
        for (var entry : inputGrammar.getProductions().entrySet()) {
            List<List<String>> clones = new ArrayList<>();
            for (List<String> rhs : entry.getValue()) {
                clones.add(new ArrayList<>(rhs));
            }
            P.put(entry.getKey(), clones);
        }

        // 1) Находим генерирующие нетерминалы
        Set<String> generating = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            for (var lhs : P.keySet()) {
                for (var rhs : P.get(lhs)) {
                    if ((rhs.size() == 1 && rhs.get(0).equals("ε")) ||
                            rhs.stream().allMatch(sym -> VT.contains(sym) || generating.contains(sym))) {
                        if (generating.add(lhs)) changed = true;
                    }
                }
            }
        } while (changed);

        if (!generating.contains(S)) {
            System.out.println("Язык грамматики пуст");
            return new Grammar(Collections.emptySet(), Collections.emptySet(), S, Collections.emptyMap());
        }

        // 2) Удаляем негенерирующие
        Set<String> VN1 = new HashSet<>(VN);
        VN1.retainAll(generating);
        P.keySet().removeIf(nt -> !VN1.contains(nt));
        for (var list : P.values()) {
            list.removeIf(rhs -> rhs.stream().anyMatch(sym -> VN.contains(sym) && !generating.contains(sym)));
        }

        // 3) Удаляем недостижимые
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        reachable.add(S);
        queue.add(S);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            for (var rhs : P.getOrDefault(cur, Collections.emptyList())) {
                for (String sym : rhs) {
                    if ((VN.contains(sym) || VT.contains(sym)) && reachable.add(sym) && VN.contains(sym)) {
                        queue.add(sym);
                    }
                }
            }
        }
        VN1.retainAll(reachable);
        VT.retainAll(reachable);
        P.keySet().removeIf(nt -> !VN1.contains(nt));
        for (var list : P.values()) {
            list.removeIf(rhs -> rhs.stream().anyMatch(sym -> (VN.contains(sym) || VT.contains(sym)) && !reachable.contains(sym)));
        }

        // 4) Удаляем ε-продукции
        Set<String> nullable = new HashSet<>();
        for (var lhs : P.keySet()) {
            for (var rhs : P.get(lhs)) {
                if (rhs.size() == 1 && rhs.get(0).equals("ε")) {
                    nullable.add(lhs);
                    break;
                }
            }
        }

        Map<String, List<List<String>>> newRules = new LinkedHashMap<>();
        for (String lhs : P.keySet()) {
            Set<List<String>> newRHSSet = new LinkedHashSet<>();
            for (List<String> rhs : P.get(lhs)) {
                if (rhs.size() == 1 && rhs.get(0).equals("ε")) continue;
                newRHSSet.add(new ArrayList<>(rhs));
                List<Integer> pos = new ArrayList<>();
                for (int i = 0; i < rhs.size(); i++) {
                    if (nullable.contains(rhs.get(i))) pos.add(i);
                }
                int combos = 1 << pos.size();
                for (int mask = 1; mask < combos; mask++) {
                    List<String> candidate = new ArrayList<>();
                    for (int i = 0; i < rhs.size(); i++) {
                        if (pos.contains(i)) {
                            int bit = pos.indexOf(i);
                            if ((mask & (1 << bit)) == 0) candidate.add(rhs.get(i));
                        } else {
                            candidate.add(rhs.get(i));
                        }
                    }
                    if (!candidate.isEmpty()) newRHSSet.add(candidate);
                }
            }
            newRules.put(lhs, new ArrayList<>(newRHSSet));
        }

        // Собираем и выводим новую грамматику
        Grammar result = new Grammar(VN1, VT, S, newRules);
        System.out.println("Нормализованная грамматика");
        System.out.println(result);
        return result;
    }

    // Для тестирования прямо из этого файла
    public static void main(String[] args) {
        Grammar base = Grammar.exampleGrammar();
        System.out.println("Исходная грамматика");
        System.out.println(base);
        transformGrammar(base);
    }
}
