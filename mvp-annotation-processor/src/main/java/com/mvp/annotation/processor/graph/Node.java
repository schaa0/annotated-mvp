package com.mvp.annotation.processor.graph;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Created by Andy on 14.04.2017.
 */

public class Node {
    public Node(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
    }
    public final TypeMirror typeMirror;
    public List<Node> nodes = new ArrayList<>();
}
