#!/usr/bin/env python3
import glob, os, re

BASE_DIR = "/home/phyroosh/.gemini/antigravity/scratch/clean-insta"
SRC_X = os.path.join(BASE_DIR, "src/X")
SMALI_DIR = os.path.join(BASE_DIR, "out/c19_smali/smali/X")

type_map = {
    "V": "void", "Z": "boolean", "B": "byte", "S": "short", "C": "char",
    "I": "int", "J": "long", "F": "float", "D": "double",
    "Ljava/lang/String;": "String", "Ljava/lang/Object;": "Object",
    "Landroid/content/Context;": "android.content.Context",
    "Landroid/view/View;": "android.view.View",
    "Landroid/view/ViewGroup;": "android.view.ViewGroup",
    "Landroid/content/Intent;": "android.content.Intent",
    "Landroid/graphics/Typeface;": "android.graphics.Typeface",
    "Ljava/util/List;": "java.util.List",
    "Ljava/io/File;": "java.io.File",
    "Ljava/io/InputStream;": "java.io.InputStream",
    "Landroid/util/AttributeSet;": "android.util.AttributeSet",
    "Landroid/app/Activity;": "android.app.Activity",
    "Landroid/view/MotionEvent;": "android.view.MotionEvent",
    "Landroid/content/DialogInterface;": "android.content.DialogInterface",
    "Lorg/json/JSONObject;": "org.json.JSONObject",
    "Lorg/json/JSONArray;": "org.json.JSONArray",
}

def smali_type_to_java(t):
    if t.startswith("["):
        return smali_type_to_java(t[1:]) + "[]"
    if t in type_map:
        return type_map[t]
    if t.startswith("L") and t.endswith(";"):
        cls = t[1:-1].replace("/", ".")
        # If it's an obfuscated class like LX.foo, map to Object
        if cls.startswith("X.") or cls.startswith("LX.") or "f2o" in cls or "VCw" in cls or "jhfeg" in cls:
            return "Object"
        return cls
    return "Object"

def parse_params(param_str):
    params = []
    i = 0
    while i < len(param_str):
        if param_str[i] == "[":
            arr_depth = 0
            while param_str[i] == "[":
                arr_depth += 1
                i += 1
            if param_str[i] == "L":
                end = param_str.find(";", i)
                t = "[" * arr_depth + param_str[i:end+1]
                i = end + 1
            else:
                t = "[" * arr_depth + param_str[i]
                i += 1
            params.append(smali_type_to_java(t))
        elif param_str[i] == "L":
            end = param_str.find(";", i)
            params.append(smali_type_to_java(param_str[i:end+1]))
            i = end + 1
        else:
            params.append(smali_type_to_java(param_str[i]))
            i += 1
    return params

def get_default_return(ret_type, params):
    if ret_type == "void":
        return ""
    if ret_type == "boolean":
        # if a boolean param exists, return it
        for idx, p in enumerate(params):
            if p == "boolean":
                return f"return p{idx};"
        return "return false;"
    if ret_type in ("int", "short", "byte", "char"):
        for idx, p in enumerate(params):
            if p == ret_type:
                return f"return p{idx};"
        return "return 0;"
    if ret_type == "long":
        for idx, p in enumerate(params):
            if p == "long":
                return f"return p{idx};"
        return "return 0L;"
    if ret_type in ("float", "double"):
        return "return 0.0f;" if ret_type == "float" else "return 0.0;"
    if ret_type == "String":
        for idx, p in enumerate(params):
            if p == "String":
                return f"return p{idx};"
        return 'return "";'
    if ret_type.endswith("[]"):
        elem = ret_type[:-2]
        return f"return new {elem}[0];"
    # Object or other class
    for idx, p in enumerate(params):
        if p == ret_type and p != "Object":
            return f"return p{idx};"
    return "return null;"

created = 0
for sf in sorted(glob.glob(os.path.join(SMALI_DIR, "A*.smali"))):
    raw_name = os.path.basename(sf).replace(".smali", "")
    if "$" in raw_name:
        continue
    java_file = os.path.join(SRC_X, f"{raw_name}.java")
    if os.path.exists(java_file):
        continue

    content = open(sf).read()
    m_super = re.search(r"\.super\s+([^;\s]+);", content)
    super_cls = m_super.group(1) if m_super else "Ljava/lang/Object;"

    # Find methods
    # match .method <flags> <name>(<params>)<ret>
    method_matches = re.findall(r"\.method\s+(.*?)\s+([^\s(]+)\((.*?)\)([^\s\n]+)", content)

    lines = ["package X;\n"]
    
    # Check if extends view or ca/da
    extends_clause = ""
    is_view = False
    if super_cls == "LX/ca":
        extends_clause = " extends ca"
        is_view = True
    elif super_cls == "LX/da":
        extends_clause = " extends da"
        is_view = True
    elif super_cls == "Landroid/widget/LinearLayout":
        extends_clause = " extends android.widget.LinearLayout"
        is_view = True
    elif super_cls == "Landroid/widget/TextView":
        extends_clause = " extends android.widget.TextView"
        is_view = True
    elif super_cls == "Landroid/widget/AutoCompleteTextView":
        extends_clause = " extends android.widget.AutoCompleteTextView"
        is_view = True
    elif super_cls == "Lcom/instagram/common/ui/base/IgFrameLayout":
        extends_clause = " extends android.widget.FrameLayout"
        is_view = True
    elif super_cls == "Lcom/instagram/common/ui/base/IgLinearLayout":
        extends_clause = " extends android.widget.LinearLayout"
        is_view = True
    elif super_cls == "Lcom/instagram/common/ui/base/IgTextView":
        extends_clause = " extends android.widget.TextView"
        is_view = True
    elif super_cls == "Landroidx/constraintlayout/widget/ConstraintLayout":
        extends_clause = " extends androidx.constraintlayout.widget.ConstraintLayout"
        is_view = True

    lines.append(f"public class {raw_name}{extends_clause} {{")

    if is_view:
        lines.append(f"    public {raw_name}(android.content.Context c) {{ super(c); }}")
        lines.append(f"    public {raw_name}(android.content.Context c, android.util.AttributeSet a) {{ super(c, a); }}")
        lines.append(f"    public {raw_name}(android.content.Context c, android.util.AttributeSet a, int i) {{ super(c, a, i); }}")

    method_signatures_seen = set()
    for flags, mname, params_str, ret_str in method_matches:
        if mname in ("<init>", "<clinit>"):
            continue
        is_static = "static" in flags
        ret_java = smali_type_to_java(ret_str)
        params_java = parse_params(params_str)
        
        # Build param string: (Type0 p0, Type1 p1)
        param_decls = [f"{pt} p{idx}" for idx, pt in enumerate(params_java)]
        param_sig = f"{mname}({', '.join(params_java)})"
        if param_sig in method_signatures_seen:
            continue
        method_signatures_seen.add(param_sig)

        ret_body = get_default_return(ret_java, params_java)
        static_kw = "static " if is_static else ""
        lines.append(f"    public {static_kw}{ret_java} {mname}({', '.join(param_decls)}) {{ {ret_body} }}")

    lines.append("}\n")

    with open(java_file, "w") as f:
        f.write("\n".join(lines))
    created += 1

print(f"[+] Generated {created} clean Java bridge classes in {SRC_X}")
