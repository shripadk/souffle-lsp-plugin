import org.eclipse.lsp4j.*;

import java.util.*;

public class ReferenceProvider {

    public ReferenceProvider() {
    }
    public List<Location> getReferences(TextDocumentPositionAndWorkDoneProgressParams params){
        return getReferences(params, true);
    }
    public List<Location> getReferences(TextDocumentPositionAndWorkDoneProgressParams params, boolean includeComponent) {
        Range cursor = new Range(params.getPosition(), params.getPosition());
        SouffleContext context = ProjectContext.getInstance().getContext(params.getTextDocument().getUri(), cursor);
        Set<Location> references = new HashSet<>();
        if (context != null) {
            SouffleSymbol currentSymbol = context.getSymbol(cursor);
            for (Map.Entry<String, SouffleContext> documentContext : ProjectContext.getInstance().getDocuments().entrySet()) {
                Optional.ofNullable(documentContext.getValue()
                                .getSymbols(currentSymbol.getName()))
                        .ifPresent(souffleSymbols -> souffleSymbols.forEach(symbol -> references.add(new Location(documentContext.getKey(), symbol.getRange()))));
                if(documentContext.getValue().getSubContext() != null){
                    for (SouffleContext ruleContext : documentContext.getValue().getSubContext().values()) {
                        if (includeComponent ||
                                (context.getKind() == SouffleContextType.COMPONENT || ruleContext.getKind() != SouffleContextType.COMPONENT)) {
                            Optional.ofNullable(ruleContext
                                            .getSymbols(currentSymbol.getName()))
                                    .ifPresent(souffleSymbols ->
                                            souffleSymbols.forEach(symbol ->{
                                                if(symbol.getURI() != null && symbol.getURI().equals(documentContext.getKey()))
                                                    references.add(new Location(documentContext.getKey(), symbol.getRange()));
                                                else if(symbol.getURI() == null)
                                                    references.add(new Location(documentContext.getKey(), symbol.getRange()));
                                            }));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(references);
    }
}