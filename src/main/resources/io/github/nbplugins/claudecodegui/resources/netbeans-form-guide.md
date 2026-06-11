# NetBeans Form Design Guide

Comprehensive rules for creating valid NetBeans GUI Designer `.form` + `.java` file pairs.
Derived from Apache NetBeans source: `GandalfPersistenceManager`, `ColorEditor`, `FontEditor`, `BorderEditor`, layout support delegates, and real `.form` samples.

---

## 1. File Creation Rules

### Always create TWO files per form class
| File | Purpose |
|------|---------|
| `ClassName.java` | Java source with `GEN-BEGIN/END` markers |
| `ClassName.form` | XML design descriptor (Matisse/Gandalf format) |

### Core rule — .java and .form must stay in sync
Every UI element declared in `.form` must have a matching variable in `GEN-BEGIN:variables` and matching init code in `GEN-BEGIN:initComponents`. A mismatch causes the Design view to fail loading.

### Package / folder rule
- Files should be in a `views` package (`.../views/ClassName.java`) for best visibility in the NetBeans Projects panel.
- Maven projects always show `.form` as a visible separate node regardless of package — this is normal cosmetic behaviour, not an error.
- Ant-based projects always hide `.form` nodes — also normal.

---

## 2. Mandatory .java Structure

### JFrame
```java
package com.example.views;

public class MyFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(MyFrame.class.getName());

    public MyFrame() {
        initComponents();
        setupComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        myButton = new javax.swing.JButton();  // alphabetical order
        myLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        myButton.setText("Click Me");
        myButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void myButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myButtonActionPerformed
        // handler body
    }//GEN-LAST:event_myButtonActionPerformed

    private void setupComponents() {
        // complex wiring outside GEN block: GridBagConstraints, custom models, etc.
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new MyFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton myButton;
    private javax.swing.JLabel myLabel;
    // End of variables declaration//GEN-END:variables
}
```

### JPanel
```java
package com.example.views;

public class MyPanel extends javax.swing.JPanel {

    public MyPanel() {
        initComponents();
        setupComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        myLabel = new javax.swing.JLabel();

        myLabel.setText("Hello");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setupComponents() { }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel myLabel;
    // End of variables declaration//GEN-END:variables
}
```

**Critical markers** — without these the Design tab will NOT open:
- `//GEN-BEGIN:initComponents` … `//GEN-END:initComponents`
- `//GEN-BEGIN:variables` … `//GEN-END:variables`
- All types must be **fully qualified** (`javax.swing.JLabel`, not `JLabel`)

### ⚠️ Official template rules (from NetBeans source templates)

**`editor-fold` desc must have spaces:** `desc=" Generated Code "` — space before and after.

**Two-phase `initComponents()` order:**
1. All `new` instantiations — alphabetical order by variable name
2. Blank line
3. Root component properties (`setDefaultCloseOperation`, `setTitle`, etc.)
4. Per-component property setters and event listeners
5. GroupLayout setup
6. `setJMenuBar(...)` if applicable
7. `pack()` — last (JFrame/JDialog/JInternalFrame only)

**Variables block is always alphabetically sorted.**

**JPanel/JInternalFrame use `this`**, not `getContentPane()`:
```java
javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
this.setLayout(layout);
```

**JDialog constructor signature:**
```java
public MyDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
}
```
JDialog default close operation is `DISPOSE_ON_CLOSE` (`value="2"` in `.form`), not `EXIT_ON_CLOSE`.

**Empty frame uses `addGap`:**
```java
layout.setHorizontalGroup(
    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGap(0, 400, Short.MAX_VALUE)
);
```

---

## 3. GEN Block Rules — What Is Forbidden

The NetBeans parser is strict. Violations cause **"cannot find sections with generated code"**.

| ❌ NOT allowed inside GEN block | ✅ Move to `setupComponents()` |
|---|---|
| `GridBagConstraints gbc = new GridBagConstraints()` | Move entirely |
| `new MyCustomClass()` or any inner class instantiation | Move entirely |
| `tableModel = new MyTableModel()` | Move entirely |
| `cmbBox.setModel(new DefaultComboBoxModel<>(...))` | Move entirely |
| Multi-line GridBagLayout wiring | Move entirely |
| `GEN-FIRST`/`GEN-LAST` inside anonymous listener body | Move to standalone method |

### Correct GEN-FIRST/GEN-LAST pattern

`GEN-FIRST`/`GEN-LAST` go on **standalone private methods placed AFTER `initComponents()`**, never inside anonymous listeners.

**❌ WRONG:**
```java
btn.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionPerformed
        doSomething();
    }//GEN-LAST:event_btnActionPerformed
});
```

**✅ CORRECT:**
```java
// Inside initComponents() — plain delegation, NO GEN markers:
btn.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnActionPerformed(evt);
    }
});

// Standalone method after initComponents():
private void btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionPerformed
    doSomething();
}//GEN-LAST:event_btnActionPerformed
```

The `handler=` attribute in `.form` XML must match the standalone method name exactly.

---

## 4. .form XML — Root Element & Form Types

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Form version="1.3" maxVersion="1.9" type="org.netbeans.modules.form.forminfo.JFrameFormInfo">
  ...
</Form>
```

### Form type attribute
| Class extends | `type=` value |
|---|---|
| `javax.swing.JFrame` | `org.netbeans.modules.form.forminfo.JFrameFormInfo` |
| `javax.swing.JPanel` | `org.netbeans.modules.form.forminfo.JPanelFormInfo` |
| `javax.swing.JDialog` | `org.netbeans.modules.form.forminfo.JDialogFormInfo` |
| `javax.swing.JInternalFrame` | `org.netbeans.modules.form.forminfo.JInternalFrameFormInfo` |

When `type=` is **omitted** (e.g., `<Form version="1.4" maxVersion="1.4">`), the form's top-level component is treated as a `JPanel`-like container (used for non-visual component panels stored in `NonVisualComponents`).

### Version numbers
The `version` attribute records the format version used when the file was saved; `maxVersion` is the highest format the file requires to load correctly. NetBeans 12+ writes `version="1.3"` or higher for GroupLayout forms and up to `maxVersion="1.9"` for newer features. Safe defaults: `version="1.3" maxVersion="1.9"`.

---

## 5. Top-level XML Sections (order matters)

```xml
<Form ...>
  <NonVisualComponents>  <!-- ButtonGroup, JMenuBar, helper JPanels not on screen -->
  </NonVisualComponents>
  <Properties>           <!-- root component properties (e.g. defaultCloseOperation) -->
  </Properties>
  <SyntheticProperties>  <!-- NetBeans-only metadata (menuBar link, size policy, etc.) -->
  </SyntheticProperties>
  <Events>               <!-- root component events (e.g. windowClosing) -->
  </Events>
  <AuxValues>            <!-- form settings (code generation flags) -->
  </AuxValues>
  <Layout>               <!-- layout of root container -->
  </Layout>
  <SubComponents>        <!-- visible components / containers -->
  </SubComponents>
</Form>
```

### NonVisualComponents vs SubComponents
- **`NonVisualComponents`** — components that exist in the Java class but are not placed on the visual canvas: `ButtonGroup`, `JMenuBar` (linked via `SyntheticProperty`), helper `JPanel`s, timers, etc.
- **`SubComponents`** — all visual components/containers that appear on the form at design time.

---

## 6. SyntheticProperties

`SyntheticProperties` are NetBeans-internal metadata that drive code generation but are not real bean properties.

```xml
<SyntheticProperties>
  <SyntheticProperty name="menuBar"          type="java.lang.String"  value="jMenuBar1"/>
  <SyntheticProperty name="formSizePolicy"   type="int"               value="1"/>
  <SyntheticProperty name="generateCenter"   type="boolean"           value="false"/>
</SyntheticProperties>
```

| Name | Type | Values / Meaning |
|------|------|-----------------|
| `menuBar` | `java.lang.String` | Name of the `JMenuBar` component declared in `NonVisualComponents` |
| `formSizePolicy` | `int` | `1` = call `pack()` (preferred); `2` = use fixed designer size |
| `generateCenter` | `boolean` | `true` = call `setLocationRelativeTo(null)` to center on screen |
| `designerSize` | `java.awt.Dimension` | Serialized byte array of the designer canvas size — do not hand-edit |

---

## 7. AuxValues (FormSettings)

All standard `AuxValues` for a new form:

```xml
<AuxValues>
  <AuxValue name="FormSettings_autoResourcing"       type="java.lang.Integer" value="0"/>
  <AuxValue name="FormSettings_autoSetComponentName" type="java.lang.Boolean" value="false"/>
  <AuxValue name="FormSettings_generateFQN"          type="java.lang.Boolean" value="true"/>
  <AuxValue name="FormSettings_generateMnemonicsCode" type="java.lang.Boolean" value="false"/>
  <AuxValue name="FormSettings_i18nAutoMode"         type="java.lang.Boolean" value="false"/>
  <AuxValue name="FormSettings_layoutCodeTarget"     type="java.lang.Integer" value="1"/>
  <AuxValue name="FormSettings_listenerGenerationStyle" type="java.lang.Integer" value="0"/>
  <AuxValue name="FormSettings_variablesLocal"       type="java.lang.Boolean" value="false"/>
  <AuxValue name="FormSettings_variablesModifier"    type="java.lang.Integer" value="2"/>
</AuxValues>
```

### variablesModifier values
| Value | Java modifier |
|-------|--------------|
| `0`   | package-private (no modifier) |
| `2`   | `private` ✅ (recommended) |
| `4`   | `protected` |

### listenerGenerationStyle values
| Value | Style |
|-------|-------|
| `0`   | Anonymous inner classes |
| `1`   | Inner class (one shared listener) |
| `3`   | Lambda expressions |

---

## 8. Layout — GroupLayout (default for new forms)

GroupLayout is the default since NetBeans 5.5. The `.form` XML encodes it as two `DimensionLayout` elements (dim="0" = horizontal, dim="1" = vertical).

### Full-frame single component
```xml
<Layout>
  <DimensionLayout dim="0">
    <Group type="103" groupAlignment="0" attributes="0">
        <Component id="myLabel" alignment="0" max="32767" attributes="0"/>
    </Group>
  </DimensionLayout>
  <DimensionLayout dim="1">
    <Group type="103" groupAlignment="0" attributes="0">
        <Component id="myLabel" alignment="0" max="32767" attributes="0"/>
    </Group>
  </DimensionLayout>
</Layout>
```

### Empty frame (no components)
```xml
<Layout>
  <DimensionLayout dim="0">
    <Group type="103" groupAlignment="0" attributes="0">
        <EmptySpace min="0" pref="400" max="32767" attributes="0"/>
    </Group>
  </DimensionLayout>
  <DimensionLayout dim="1">
    <Group type="103" groupAlignment="0" attributes="0">
        <EmptySpace min="0" pref="300" max="32767" attributes="0"/>
    </Group>
  </DimensionLayout>
</Layout>
```

### GroupLayout XML element reference
| Element | Key attributes | Meaning |
|---------|---------------|---------|
| `Group` | `type="103"` | Parallel group |
| `Group` | `type="102"` | Sequential group |
| `Component` | `id`, `min`, `pref`, `max` | Component size in group; `max="32767"` = `Short.MAX_VALUE` = grow |
| `EmptySpace` | `min`, `pref`, `max` | Gap / padding; `-2` = default gap |
| `Group` | `groupAlignment="0"` | LEADING; `"1"`= TRAILING; `"2"`= CENTER; `"3"`= BASELINE |

### Matching Java code
```java
javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
getContentPane().setLayout(layout);
layout.setHorizontalGroup(
    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
);
layout.setVerticalGroup(
    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addComponent(myLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
);
```

---

## 9. Layout — Other Layout Managers

### BorderLayout
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout"/>
<SubComponents>
  <Container class="javax.swing.JPanel" name="mainPanel">
    <Constraints>
      <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout"
                  value="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$BorderConstraintsDescription">
        <BorderConstraints direction="Center"/>
      </Constraint>
    </Constraints>
    ...
  </Container>
</SubComponents>
```

`direction` values: `"Center"`, `"North"`, `"South"`, `"East"`, `"West"`

Java equivalent: `add(mainPanel, java.awt.BorderLayout.CENTER);`

### GridBagLayout
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"/>
<SubComponents>
  <Component class="javax.swing.JLabel" name="myLabel">
    <Constraints>
      <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                  value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
        <GridBagConstraints gridX="-1" gridY="-1" gridWidth="1" gridHeight="1"
                            fill="2" ipadX="0" ipadY="0"
                            insetsTop="0" insetsLeft="0" insetsBottom="6" insetsRight="6"
                            anchor="17" weightX="0.0" weightY="0.0"/>
      </Constraint>
    </Constraints>
  </Component>
</SubComponents>
```

`fill` values: `0`=NONE, `1`=BOTH, `2`=HORIZONTAL, `3`=VERTICAL  
`anchor` values: `10`=CENTER, `17`=EAST (LINE_END), `13`=WEST (LINE_START), `11`=NORTH, `15`=SOUTH

### FlowLayout
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignFlowLayout"/>
```
No constraints needed — components are added in order.

### JScrollPane layout (single child)
```xml
<Container class="javax.swing.JScrollPane" name="scrollPane">
  <AuxValues>
    <AuxValue name="autoScrollPane" type="java.lang.Boolean" value="true"/>
  </AuxValues>
  <Layout class="org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout"/>
  <SubComponents>
    <Component class="javax.swing.JList" name="myList">
      ...
    </Component>
  </SubComponents>
</Container>
```

- `autoScrollPane="true"` means NetBeans auto-wrapped the child in a scroll pane from the palette.
- `JScrollPaneSupportLayout` accepts **exactly one** child; adding more is undefined.
- Java equivalent: `scrollPane.setViewportView(myList);`

### JTabbedPane layout
```xml
<Container class="javax.swing.JTabbedPane" name="tabbedPane">
  <Layout class="org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout"/>
  <SubComponents>
    <Container class="javax.swing.JPanel" name="tab1Panel">
      <Constraints>
        <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout"
                    value="org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout$JTabbedPaneConstraintsDescription">
          <JTabbedPaneConstraints tabName="Tab 1">
            <Property name="tabTitle" type="java.lang.String" value="Tab 1"/>
          </JTabbedPaneConstraints>
        </Constraint>
      </Constraints>
      <Layout>...</Layout>
      <SubComponents>...</SubComponents>
    </Container>
  </SubComponents>
</Container>
```

Java equivalent: `tabbedPane.addTab("Tab 1", tab1Panel);`

---

## 10. Color Properties — ⚠️ CRITICAL (verified from NetBeans source)

> **Source verified**: `ColorEditor.readFromXML` in `platform/o.n.core/src/org/netbeans/beaninfo/editors/ColorEditor.java` (apache/netbeans).
> It calls `Integer.parseInt(red, 16)` — **radix 16**. The `type` attribute is read first with no null-check; missing it throws `IOException`.

### THE ONE CORRECT FORMAT — memorize this, never deviate

```xml
<Property name="background" type="java.awt.Color" editor="org.netbeans.beaninfo.editors.ColorEditor">
  <Color red="f" green="4c" blue="81" type="rgb"/>
</Property>
```

**Three mandatory rules — ALL must hold or NetBeans throws an exception:**

1. **`type="rgb"` is required** — missing it → `IOException` (NPE inside `readFromXML`)
2. **Values are lowercase hex strings** — `Integer.parseInt(value, 16)` is used — e.g. `"ff"` not `"255"`
3. **No `alpha` attribute** — causes `IllegalArgumentException` on NetBeans 23+

Applies to: `background`, `foreground`, `caretColor`, `selectionColor`, `selectionBackground`, etc.

### Hex conversion quick-reference
| RGB decimal | hex | RGB decimal | hex |
|-------------|-----|-------------|-----|
| 255 | `ff` | 128 | `80` |
| 240 | `f0` | 100 | `64` |
| 220 | `dc` | 96 | `60` |
| 200 | `c8` | 80 | `50` |
| 192 | `c0` | 76 | `4c` |
| 185 | `b9` | 68 | `44` |
| 180 | `b4` | 57 | `39` |
| 174 | `ae` | 44 | `2c` |
| 165 | `a5` | 41 | `29` |
| 230 | `e6` | 39 | `27` |
| 245 | `f5` | 34 | `22` |
| 250 | `fa` | 30 | `1e` |
| 129 | `81` | 24 | `18` |
| 126 | `7e` | 15 | `f` |
| 0 | `0` | | |

**To convert**: `printf '%x' <decimal>` or Python `hex(n)[2:]`

### What causes errors
| Wrong format | Exception | Why |
|---|---|---|
| Missing `type="rgb"` | `IOException` | `readFromXML` calls `.getNodeValue()` on null → NPE → `throw new IOException()` |
| `red="255"` (decimal > 0xff) | `IllegalArgumentException` | `Integer.parseInt("255", 16)` = 597, out of 0–255 range |
| `red="0.0"` (float) | `NumberFormatException` | `parseInt` cannot parse decimal point |
| `alpha="255"` attribute | `IllegalArgumentException` | NetBeans 23+ rejects alpha channel in this context |

---

## 11. Font Properties

```xml
<Property name="font" type="java.awt.Font" editor="org.netbeans.beaninfo.editors.FontEditor">
  <Font name="SansSerif" size="14" style="1"/>
</Property>
```

### style values
| Value | Meaning |
|-------|---------|
| `0` | Plain |
| `1` | Bold |
| `2` | Italic |
| `3` | Bold + Italic |

### Common font names
`SansSerif`, `Serif`, `Monospaced`, `Dialog`, `DialogInput`  
System fonts (`Arial`, `Tahoma`, etc.) work only if installed on the target machine.

Java equivalent: `component.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14)); // NOI18N`

---

## 12. Border Properties

```xml
<Property name="border" type="javax.swing.border.Border" editor="org.netbeans.modules.form.editors2.BorderEditor">
  <Border info="org.netbeans.modules.form.compat2.border.TitledBorderInfo">
    <TitledBorder title="Section Title"/>
  </Border>
</Property>
```

### Border type reference
| Border type | `info=` value | Inner element |
|-------------|--------------|---------------|
| TitledBorder | `...TitledBorderInfo` | `<TitledBorder title="..."/>` |
| EmptyBorder | `...EmptyBorderInfo` | `<EmptyBorder top="n" left="n" bottom="n" right="n"/>` |
| LineBorder | `...LineBorderInfo` | `<LineBorder/>` + color property |
| EtchedBorder | `...EtchedBorderInfo` | `<EtchetBorder/>` ⚠️ typo — see below |
| BevelBorder | `...BevelBorderInfo` | `<BevelBorder bevelType="0"/>` (0=RAISED, 1=LOWERED) |
| MatteBorder | `...MatteBorderInfo` | `<MatteBorder top="n" left="n" bottom="n" right="n"/>` |
| CompoundBorder | `...CompoundBorderInfo` | nested `<Border>` elements |

### ⚠️ EtchedBorder typo
`BorderEditor.readEtchedBorder()` looks for `"EtchetBorder"` (missing the 'd') — a **known typo in NetBeans source**.

**Wrong:** `<EtchedBorder/>` → `IOException: Invalid format: missing "EtchetBorder" element`  
**Correct:** `<EtchetBorder/>`

### EmptyBorder example
```xml
<Border info="org.netbeans.modules.form.compat2.border.EmptyBorderInfo">
  <EmptyBorder bottom="12" left="12" right="12" top="12"/>
</Border>
```

---

## 13. Dimension Property

```xml
<Property name="minimumSize" type="java.awt.Dimension" editor="org.netbeans.beaninfo.editors.DimensionEditor">
  <Dimension value="[297, 200]"/>
</Property>
```

Format: `[width, height]` with a space after the comma. Applies to `minimumSize`, `maximumSize`, `preferredSize`.

---

## 14. Component Reference (labelFor, buttonGroup)

```xml
<Property name="labelFor" type="java.awt.Component" editor="org.netbeans.modules.form.ComponentChooserEditor">
  <ComponentRef name="targetComponentName"/>
</Property>
```

Used for `labelFor` on `JLabel`, and for `buttonGroup` on radio buttons.

---

## 15. Component Properties Reference

### JLabel
```xml
<Component class="javax.swing.JLabel" name="myLabel">
  <Properties>
    <Property name="text"                type="java.lang.String" value="My Text"/>
    <Property name="horizontalAlignment" type="int"              value="0"/>
    <Property name="verticalAlignment"   type="int"              value="0"/>
    <Property name="font"   .../>
    <Property name="foreground" .../>
    <Property name="background" .../>
    <Property name="opaque" type="boolean" value="true"/>
  </Properties>
</Component>
```

### JButton
```xml
<Component class="javax.swing.JButton" name="myButton">
  <Properties>
    <Property name="text" type="java.lang.String" value="Click Me"/>
  </Properties>
  <Events>
    <EventHandler event="actionPerformed" listener="java.awt.event.ActionListener"
                  parameters="java.awt.event.ActionEvent" handler="myButtonActionPerformed"/>
  </Events>
</Component>
```

### JTextField
```xml
<Component class="javax.swing.JTextField" name="myTextField">
  <Properties>
    <Property name="text"    type="java.lang.String" value=""/>
    <Property name="columns" type="int"              value="20"/>
  </Properties>
</Component>
```

### JTextArea
```xml
<Component class="javax.swing.JTextArea" name="myTextArea">
  <Properties>
    <Property name="columns"       type="int"     value="20"/>
    <Property name="rows"          type="int"     value="5"/>
    <Property name="lineWrap"      type="boolean" value="true"/>
    <Property name="wrapStyleWord" type="boolean" value="true"/>
  </Properties>
</Component>
```

JTextArea is almost always wrapped in a JScrollPane (see Section 9).

### JPanel (container)
```xml
<Container class="javax.swing.JPanel" name="myPanel">
  <Properties>
    <Property name="border" .../>
    <Property name="background" .../>
  </Properties>
  <Layout>...</Layout>
  <SubComponents>...</SubComponents>
</Container>
```

### JCheckBox
```xml
<Component class="javax.swing.JCheckBox" name="myCheckBox">
  <Properties>
    <Property name="text"     type="java.lang.String" value="Enable feature"/>
    <Property name="selected" type="boolean"          value="false"/>
  </Properties>
</Component>
```

### JRadioButton + ButtonGroup
ButtonGroup must be declared in `NonVisualComponents` (it is non-visual):

```xml
<NonVisualComponents>
  <Component class="javax.swing.ButtonGroup" name="buttonGroup1">
  </Component>
</NonVisualComponents>
```

Then reference it in each radio button:
```xml
<Component class="javax.swing.JRadioButton" name="radioA">
  <Properties>
    <Property name="buttonGroup" type="javax.swing.ButtonGroup"
              editor="org.netbeans.modules.form.RADComponent$ButtonGroupPropertyEditor">
      <ComponentRef name="buttonGroup1"/>
    </Property>
    <Property name="text" type="java.lang.String" value="Option A"/>
    <Property name="selected" type="boolean" value="true"/>
  </Properties>
</Component>
```

Java GEN block: `buttonGroup1 = new javax.swing.ButtonGroup();`  
Variables block: `private javax.swing.ButtonGroup buttonGroup1;`

### JComboBox — ⚠️ CRITICAL: no model in .form

`org.netbeans.modules.form.editors2.JComboBoxModelEditor` does **not** exist in modern NetBeans — using `editor=` causes `ClassNotFoundException`. Omit the `model` property entirely from `.form` and set it in `setupComponents()`.

**❌ WRONG:**
```xml
<Property name="model" type="javax.swing.ComboBoxModel"
          editor="org.netbeans.modules.form.editors2.JComboBoxModelEditor">
  <StringArray count="3">...</StringArray>
</Property>
```

**✅ CORRECT — no model in .form:**
```xml
<Component class="javax.swing.JComboBox" name="myCombo">
</Component>
```

In `setupComponents()`:
```java
myCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"A", "B", "C"}));
```

Use raw type `class="javax.swing.JComboBox"` (no generics). Variables block: `private javax.swing.JComboBox myCombo;`

### JTable
```xml
<Component class="javax.swing.JTable" name="myTable">
  <Properties>
    <Property name="model" type="javax.swing.table.TableModel"
              editor="org.netbeans.modules.form.editors2.TableModelEditor">
      <Table columnCount="3" rowCount="4">
        <Column editable="true" title="Column 1" type="java.lang.Object"/>
        <Column editable="true" title="Column 2" type="java.lang.Object"/>
        <Column editable="true" title="Column 3" type="java.lang.Object"/>
      </Table>
    </Property>
    <Property name="autoResizeMode" type="int" value="4"/>
  </Properties>
</Component>
```

JTable is almost always wrapped in a `JScrollPane`.

---

## 16. JMenuBar — Full Pattern

JMenuBar lives in `NonVisualComponents` and is linked to the JFrame via `SyntheticProperty name="menuBar"`.

```xml
<NonVisualComponents>
  <Menu class="javax.swing.JMenuBar" name="jMenuBar1">
    <SubComponents>
      <Menu class="javax.swing.JMenu" name="fileMenu">
        <Properties>
          <Property name="text" type="java.lang.String" value="File"/>
          <Property name="mnemonic" type="int" value="70"/>
        </Properties>
        <SubComponents>
          <MenuItem class="javax.swing.JMenuItem" name="exitMenuItem">
            <Properties>
              <Property name="text" type="java.lang.String" value="Exit"/>
            </Properties>
            <Events>
              <EventHandler event="actionPerformed" listener="java.awt.event.ActionListener"
                            parameters="java.awt.event.ActionEvent" handler="exitMenuItemActionPerformed"/>
            </Events>
          </MenuItem>
        </SubComponents>
      </Menu>
    </SubComponents>
  </Menu>
</NonVisualComponents>
<SyntheticProperties>
  <SyntheticProperty name="menuBar" type="java.lang.String" value="jMenuBar1"/>
</SyntheticProperties>
```

### Menu element types
| XML element | Java class |
|-------------|-----------|
| `<Menu class="javax.swing.JMenuBar">` | `JMenuBar` (root) |
| `<Menu class="javax.swing.JMenu">` | `JMenu` |
| `<MenuItem class="javax.swing.JMenuItem">` | `JMenuItem` |
| `<MenuItem class="javax.swing.JCheckBoxMenuItem">` | `JCheckBoxMenuItem` |
| `<MenuItem class="javax.swing.JRadioButtonMenuItem">` | `JRadioButtonMenuItem` |
| `<MenuItem class="javax.swing.JSeparator">` | Separator |

Java GEN block:
```java
jMenuBar1 = new javax.swing.JMenuBar();
fileMenu  = new javax.swing.JMenu();
exitMenuItem = new javax.swing.JMenuItem();
// ...
setJMenuBar(jMenuBar1);
```

Variables block:
```java
private javax.swing.JMenu fileMenu;
private javax.swing.JMenuItem exitMenuItem;
private javax.swing.JMenuBar jMenuBar1;
```

---

## 17. Event Handling

### .form XML
```xml
<Events>
  <EventHandler event="actionPerformed"
                listener="java.awt.event.ActionListener"
                parameters="java.awt.event.ActionEvent"
                handler="myButtonActionPerformed"/>
</Events>
```

### Root-level events (JFrame)
```xml
<Events>
  <EventHandler event="windowClosing" listener="java.awt.event.WindowListener"
                parameters="java.awt.event.WindowEvent" handler="exitForm"/>
</Events>
```

### Common event signatures
| Component | event | listener | parameters |
|-----------|-------|----------|-----------|
| JButton | `actionPerformed` | `java.awt.event.ActionListener` | `java.awt.event.ActionEvent` |
| JTextField | `actionPerformed` | `java.awt.event.ActionListener` | `java.awt.event.ActionEvent` |
| JTextField | `keyReleased` | `java.awt.event.KeyListener` | `java.awt.event.KeyEvent` |
| JComboBox | `actionPerformed` | `java.awt.event.ActionListener` | `java.awt.event.ActionEvent` |
| JCheckBox | `actionPerformed` | `java.awt.event.ActionListener` | `java.awt.event.ActionEvent` |
| JList | `valueChanged` | `javax.swing.event.ListSelectionListener` | `javax.swing.event.ListSelectionEvent` |
| JFrame (root) | `windowClosing` | `java.awt.event.WindowListener` | `java.awt.event.WindowEvent` |

---

## 18. Property preCode / postCode

Properties can carry `preCode` and `postCode` attributes that wrap the generated setter call in arbitrary Java:

```xml
<Property name="enabled" type="boolean" value="false" preCode="if (false) {" postCode="}"/>
```

Generated Java: `if (false) { myComponent.setEnabled(false); }`

Used rarely, typically by NetBeans itself for design-time-only placeholders.

---

## 19. Alignment & Spacing Quick Reference

### horizontalAlignment (JLabel, JTextField, JButton)
| Value | Constant |
|-------|----------|
| `0` | CENTER |
| `2` | LEFT |
| `4` | RIGHT |
| `10` | LEADING |
| `11` | TRAILING |

### verticalAlignment (JLabel)
| Value | Constant |
|-------|----------|
| `0` | CENTER |
| `1` | TOP |
| `3` | BOTTOM |

### defaultCloseOperation (JFrame)
| Value | Constant |
|-------|----------|
| `0` | DO_NOTHING_ON_CLOSE |
| `1` | HIDE_ON_CLOSE |
| `2` | DISPOSE_ON_CLOSE |
| `3` | EXIT_ON_CLOSE ✅ |

---

## 20. Complete Working JFrame Template

### `views/MyFrame.form`
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Form version="1.3" maxVersion="1.9" type="org.netbeans.modules.form.forminfo.JFrameFormInfo">
  <Properties>
    <Property name="defaultCloseOperation" type="int" value="3"/>
    <Property name="title" type="java.lang.String" value="My Application"/>
  </Properties>
  <SyntheticProperties>
    <SyntheticProperty name="formSizePolicy" type="int" value="1"/>
    <SyntheticProperty name="generateCenter" type="boolean" value="false"/>
  </SyntheticProperties>
  <AuxValues>
    <AuxValue name="FormSettings_autoResourcing"          type="java.lang.Integer" value="0"/>
    <AuxValue name="FormSettings_autoSetComponentName"    type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_generateFQN"             type="java.lang.Boolean" value="true"/>
    <AuxValue name="FormSettings_generateMnemonicsCode"   type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_i18nAutoMode"            type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_layoutCodeTarget"        type="java.lang.Integer" value="1"/>
    <AuxValue name="FormSettings_listenerGenerationStyle" type="java.lang.Integer" value="0"/>
    <AuxValue name="FormSettings_variablesLocal"          type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_variablesModifier"       type="java.lang.Integer" value="2"/>
  </AuxValues>
  <Layout>
    <DimensionLayout dim="0">
      <Group type="103" groupAlignment="0" attributes="0">
          <Component id="titleLabel" alignment="0" max="32767" attributes="0"/>
      </Group>
    </DimensionLayout>
    <DimensionLayout dim="1">
      <Group type="103" groupAlignment="0" attributes="0">
          <Component id="titleLabel" alignment="0" max="32767" attributes="0"/>
      </Group>
    </DimensionLayout>
  </Layout>
  <SubComponents>
    <Component class="javax.swing.JLabel" name="titleLabel">
      <Properties>
        <Property name="font" type="java.awt.Font" editor="org.netbeans.beaninfo.editors.FontEditor">
          <Font name="SansSerif" size="24" style="1"/>
        </Property>
        <Property name="foreground" type="java.awt.Color" editor="org.netbeans.beaninfo.editors.ColorEditor">
          <Color red="0" green="80" blue="0" type="rgb"/>
        </Property>
        <Property name="horizontalAlignment" type="int" value="0"/>
        <Property name="text" type="java.lang.String" value="Hello World"/>
      </Properties>
    </Component>
  </SubComponents>
</Form>
```

### `views/MyFrame.java`
```java
package com.example.views;

public class MyFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(MyFrame.class.getName());

    public MyFrame() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("My Application");

        titleLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 128, 0));
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Hello World");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new MyFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
```

---

## 21. MCP Workflow Checklist

- [ ] Get project package from `pom.xml` → `<groupId>` + `<artifactId>`
- [ ] Create `ClassName.form` in `.../views/` package folder
- [ ] Create `ClassName.java` in `.../views/` package folder with correct `package` declaration
- [ ] `.form` component `id` names must match variable names in `GEN-BEGIN:variables`
- [ ] Use **hex string** color values in `.form` with **`type="rgb"`** (never decimal integers, never float) — e.g. `<Color red="ff" green="ff" blue="ff" type="rgb"/>` — missing `type` causes `IOException`
- [ ] Use fully-qualified class names in `initComponents()` (`javax.swing.*`, `java.awt.*`)
- [ ] **Never** put `GEN-FIRST`/`GEN-LAST` inside anonymous listener bodies — use standalone methods
- [ ] **Never** declare `GridBagConstraints`, custom model/table objects, or inner class instances inside `GEN block` — all go in `setupComponents()`
- [ ] **Never** include `model` property for `JComboBox` in `.form` — set via `setupComponents()`
- [ ] `JComboBox` in `.form`: raw type `class="javax.swing.JComboBox"` (no generics); same in GEN variables block
- [ ] `ButtonGroup` declared in `<NonVisualComponents>` (non-visual), referenced via `<ComponentRef>`
- [ ] `JMenuBar` declared in `<NonVisualComponents>` + linked via `<SyntheticProperty name="menuBar" value="..."/>`
- [ ] `JScrollPane` children use `JScrollPaneSupportLayout` + `autoScrollPane` AuxValue
- [ ] `JTabbedPane` children use `JTabbedPaneSupportLayout` + `JTabbedPaneConstraints` with `tabTitle`
- [ ] `EtchedBorder` uses `<EtchetBorder/>` (typo in NetBeans source — intentional)
- [ ] **NEVER put `<!-- ... -->` XML comments anywhere in the `.form` file** — causes `PersistenceException: Missing attributes of component element` (see Section 23)
- [ ] Every child of a `DesignGridBagLayout` container has a full `<Constraints><GridBagConstraints .../>` block — without this, design view collapses all items into one row (see Section 24)
- [ ] Design view = runtime: all fonts/colors/borders/sizes declared in `.form`; GridBagConstraints in `.form` AND `setupComponents()`; ComboBox models / custom TableModel / cell editors only in `setupComponents()`
- [ ] After writing `.form`, verify no comments: `Select-String -Path "*.form" -Pattern "<!--"` — must return no matches
- [ ] Call `mcp0_openFile` (NetBeans MCP tool) to open `.java` in NetBeans after creation
- [ ] User closes and reopens the tab in NetBeans to load Design view fresh

---

## 23. ⚠️ CRITICAL: No XML Comments Inside .form Files

**XML comments (`<!-- ... -->`) inside `.form` files cause `PersistenceException: Missing attributes of component element` and the Design view will fail to load.**

### Root cause (from NetBeans source `GandalfPersistenceManager.java`)

The parser iterates `childNodes` and calls `restoreComponent()` on every node. It skips `TEXT_NODE` but does **not** skip `COMMENT_NODE`. A comment node returns `null` from `getAttributes()`, which immediately triggers the exception:

```java
// From GandalfPersistenceManager.restoreComponent():
org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
if (attrs == null) {  // comment nodes hit this — they have no attributes
    throw new PersistenceException("Missing attributes of component element");
}
```

### Rule

**NEVER put `<!-- ... -->` comments anywhere inside a `.form` file.** Not inside `<SubComponents>`, not inside `<Properties>`, not anywhere. The file must be comment-free.

**❌ WRONG — causes PersistenceException:**
```xml
<SubComponents>
  <!-- This is the toolbar -->
  <Container class="javax.swing.JPanel" name="pnlToolbar">
    ...
  </Container>
</SubComponents>
```

**✅ CORRECT — no comments at all:**
```xml
<SubComponents>
  <Container class="javax.swing.JPanel" name="pnlToolbar">
    ...
  </Container>
</SubComponents>
```

### Verification command
After writing any `.form` file, always verify:
```powershell
Select-String -Path "MyPanel.form" -Pattern "<!--"
# Must return NO matches
```

---

## 24. GridBagLayout in .form — Constraints Are Mandatory

When a container uses `DesignGridBagLayout`, every child component **must** declare a full `<Constraints>` block with `<GridBagConstraints>`. Without constraints, the design view puts all components in a single row with default positions — completely different from the runtime appearance where `setupComponents()` applies the real `GridBagConstraints`.

### Rule

**Every child of a `DesignGridBagLayout` container must have explicit `<Constraints>` in the `.form`, matching exactly the `gbc.gridx / gbc.gridy / gbc.fill / gbc.weightx / gbc.insets` values in `setupComponents()`.**

### Full working pattern — vertical stack (single-column GridBag)

```xml
<Container class="javax.swing.JPanel" name="myPanel">
  <Layout class="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"/>
  <SubComponents>

    <Component class="javax.swing.JLabel" name="lblTitle">
      <Properties>
        <Property name="text" type="java.lang.String" value="Title:"/>
      </Properties>
      <Constraints>
        <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                    value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
          <GridBagConstraints gridX="0" gridY="0" gridWidth="1" gridHeight="1"
                              fill="2" ipadX="0" ipadY="0"
                              insetsTop="8" insetsLeft="10" insetsBottom="8" insetsRight="10"
                              anchor="17" weightX="1.0" weightY="0.0"/>
        </Constraint>
      </Constraints>
    </Component>

    <Component class="javax.swing.JTextField" name="txtValue">
      <Constraints>
        <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                    value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
          <GridBagConstraints gridX="0" gridY="1" gridWidth="1" gridHeight="1"
                              fill="2" ipadX="0" ipadY="0"
                              insetsTop="8" insetsLeft="10" insetsBottom="8" insetsRight="10"
                              anchor="17" weightX="1.0" weightY="0.0"/>
        </Constraint>
      </Constraints>
    </Component>

  </SubComponents>
</Container>
```

### GridBagConstraints attribute quick reference

| Attribute | Meaning | Common values |
|-----------|---------|---------------|
| `gridX` | Column | `0` = first column |
| `gridY` | Row | `0`, `1`, `2`... each component one row down |
| `gridWidth` | Columns spanned | `1` = single column |
| `gridHeight` | Rows spanned | `1` = single row |
| `fill` | Fill direction | `0`=NONE, `1`=BOTH, `2`=HORIZONTAL, `3`=VERTICAL |
| `weightX` | Horizontal grow | `1.0` = fill panel width; `0.0` = don't grow |
| `weightY` | Vertical grow | `1.0` = fill; `0.0` = shrink to content |
| `anchor` | Alignment | `10`=CENTER, `13`=WEST, `17`=EAST, `11`=NORTH, `15`=SOUTH |
| `insetsTop/Left/Bottom/Right` | Padding in pixels | e.g. `8` / `10` / `8` / `10` |

### Multi-column row (combo + textfield + label side by side)

```xml
<Component class="javax.swing.JComboBox" name="cmbMode">
  <Constraints>
    <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
      <GridBagConstraints gridX="0" gridY="0" gridWidth="1" gridHeight="1"
                          fill="2" ipadX="0" ipadY="0"
                          insetsTop="0" insetsLeft="0" insetsBottom="0" insetsRight="4"
                          anchor="17" weightX="0.5" weightY="0.0"/>
    </Constraint>
  </Constraints>
</Component>
<Component class="javax.swing.JTextField" name="txtAmount">
  <Constraints>
    <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
      <GridBagConstraints gridX="1" gridY="0" gridWidth="1" gridHeight="1"
                          fill="2" ipadX="0" ipadY="0"
                          insetsTop="0" insetsLeft="0" insetsBottom="0" insetsRight="4"
                          anchor="17" weightX="0.3" weightY="0.0"/>
    </Constraint>
  </Constraints>
</Component>
<Component class="javax.swing.JLabel" name="lblResult">
  <Constraints>
    <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
      <GridBagConstraints gridX="2" gridY="0" gridWidth="1" gridHeight="1"
                          fill="2" ipadX="0" ipadY="0"
                          insetsTop="0" insetsLeft="4" insetsBottom="0" insetsRight="0"
                          anchor="17" weightX="0.2" weightY="0.0"/>
    </Constraint>
  </Constraints>
</Component>
```

### Vertical filler (push all rows to top)

Add as the last child with `weightY="1.0"` and `fill="1"` (BOTH):

```xml
<Component class="javax.swing.JPanel" name="fillerPanel">
  <Constraints>
    <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"
                value="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription">
      <GridBagConstraints gridX="0" gridY="13" gridWidth="1" gridHeight="1"
                          fill="1" ipadX="0" ipadY="0"
                          insetsTop="0" insetsLeft="0" insetsBottom="0" insetsRight="0"
                          anchor="10" weightX="1.0" weightY="1.0"/>
    </Constraint>
  </Constraints>
</Component>
```

Java equivalent in `setupComponents()`:
```java
java.awt.GridBagConstraints gbcFill = new java.awt.GridBagConstraints();
gbcFill.gridx = 0; gbcFill.gridy = 13;
gbcFill.weightx = 1.0; gbcFill.weighty = 1.0;
gbcFill.fill = java.awt.GridBagConstraints.BOTH;
myPanel.add(new javax.swing.JPanel(), gbcFill);
```

---

## 25. Design View vs Runtime — Sync Rules

When a panel uses `setupComponents()` for layout wiring (GridBagConstraints, ComboBox models, custom TableModel, borders, fonts, colors), the **Design view only shows what is declared in the `.form` and `initComponents()` GEN block**. `setupComponents()` is never executed at design time.

### What goes where

| Property | In `.form` + `initComponents()` GEN block | In `setupComponents()` only |
|----------|------------------------------------------|----------------------------|
| Fonts, colors, borders, text, preferred sizes | ✅ Yes | Also fine here |
| `GridBagConstraints` wiring | ✅ Yes (as `<Constraints>` in `.form`) | Also required here |
| `JComboBox.setModel(...)` | ❌ Never | ✅ Yes |
| Custom `TableModel` subclass | ❌ Never | ✅ Yes |
| `DefaultTableModel` (anonymous subclass with `isCellEditable`) | ✅ Yes (in GEN block) | Optional |
| Cell editors / cell renderers | ❌ Never | ✅ Yes |
| `KeyListener` on `JComboBox` for Enter-key navigation | ❌ Never | ✅ Yes |
| Filler `JPanel` for GridBag vertical push | ✅ As `<Component class="javax.swing.JPanel">` in `.form` | Also required |

### Golden rule

**The `.form` + `initComponents()` GEN block must alone produce a design view that looks identical to the runtime.** Every visual property that contributes to layout or appearance must be declared in both places. `setupComponents()` is for things the GEN block forbids (custom instances, GridBagConstraints objects, ComboBox models) — not for styling.

### Variables block must be alphabetically sorted

The `GEN-BEGIN:variables` block must list all variables in **alphabetical order**. NetBeans enforces this when it regenerates the block. Mismatch causes sync errors.

```java
// Variables declaration - do not modify//GEN-BEGIN:variables
private javax.swing.JButton btnAddRow;
private javax.swing.JButton btnCheckout;
private javax.swing.JButton btnClearAll;
// ... alphabetical order ...
// End of variables declaration//GEN-END:variables
```

---

## 26. JToolBar — Design View Hard Limitation

### ⚠️ NetBeans Matisse CANNOT add children to JToolBar visually

This is a confirmed NetBeans GUI builder limitation. The Matisse editor treats `JToolBar` as a **leaf component** (`<Component>` tag), not a container (`<Container>` tag). If you manually add `<SubComponents>` inside a `<Component class="javax.swing.JToolBar">` in the `.form` XML, **NetBeans will strip them out the next time it touches the file**.

**There is no way to see toolbar buttons in the Design view.** The toolbar will always appear empty in design view.

### Correct pattern — NonVisualComponents + setupToolBar()

Declare the button variables as `<NonVisualComponents>` in the `.form` so NetBeans generates the field declarations in the GEN variables block. Build the actual toolbar entirely in `setupToolBar()` using those variables.

**In `.form` — declare buttons as NonVisualComponents:**
```xml
<NonVisualComponents>
  <Component class="javax.swing.JButton" name="btnDashboard">
    <Properties>
      <Property name="text" type="java.lang.String" value="Dashboard"/>
    </Properties>
  </Component>
  <Component class="javax.swing.JButton" name="btnPos">
    <Properties>
      <Property name="text" type="java.lang.String" value="POS"/>
    </Properties>
  </Component>
</NonVisualComponents>
```

**In GEN block `initComponents()`** — NetBeans auto-generates instantiation + setText only (no add to toolbar):
```java
btnDashboard = new javax.swing.JButton();
btnPos = new javax.swing.JButton();
btnDashboard.setText("Dashboard");
btnPos.setText("POS");
```

**In `setupToolBar()`** — full toolbar construction using the GEN-declared variables:
```java
private void setupToolBar() {
    toolBar.removeAll();  // safe — toolbar has no children from GEN
    toolBar.setBackground(new java.awt.Color(15, 76, 129));
    toolBar.setFloatable(false);

    javax.swing.JButton[] navBtns = {btnDashboard, btnPos};
    for (int i = 0; i < navBtns.length; i++) {
        navBtns[i].setForeground(java.awt.Color.WHITE);
        navBtns[i].setOpaque(false);
        navBtns[i].setContentAreaFilled(false);
        toolBar.add(navBtns[i]);
        if (i < navBtns.length - 1) {
            javax.swing.JSeparator sep = new javax.swing.JSeparator(javax.swing.JSeparator.VERTICAL);
            sep.setMaximumSize(new java.awt.Dimension(1, 24));
            toolBar.add(sep);
        }
    }
    toolBar.add(javax.swing.Box.createHorizontalGlue());
}
```

### Why this is the right pattern
- Variables are generated in the GEN variables block from `<NonVisualComponents>` — compiler is happy.
- `setupToolBar()` uses `removeAll()` which is safe because GEN never added anything to the toolbar.
- NetBeans never touches the toolbar children because there are none declared.
- **The toolbar will be empty/grey in design view — this is unavoidable.** Do NOT attempt to put toolbar buttons as `<SubComponents>` of `<Component class="javax.swing.JToolBar">` — NetBeans will delete them.

---

## 27. JFrame windowClosing Event

To wire a `windowClosing` handler so the Design view knows about it, declare it in the `<Events>` block on the **root `<Form>` element**, not on a sub-component:

```xml
<Form version="1.3" ...>
  <Properties>...</Properties>
  <Events>
    <EventHandler event="windowClosing" listener="java.awt.event.WindowListener"
                  parameters="java.awt.event.WindowEvent" handler="formWindowClosing"/>
  </Events>
  ...
</Form>
```

In the GEN block, this generates:
```java
addWindowListener(new java.awt.event.WindowAdapter() {
    public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
    }
});
```

And the standalone handler:
```java
private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    confirmExit();
}//GEN-LAST:event_formWindowClosing
```

> If `DO_NOTHING_ON_CLOSE` + `windowClosing` handler is NOT declared in `.form`, the Design view won't show the event wiring, and NetBeans may regenerate the GEN block without the `addWindowListener` call.

---

## 28. BorderLayout and GridBagLayout XML Reference

### BorderLayout
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout"/>
```
Constraint: `<BorderConstraints direction="Center"/>` (values: `Center`, `North`, `South`, `East`, `West`)

### GridBagLayout
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout"/>
```
Constraint:
```xml
<GridBagConstraints gridX="-1" gridY="-1" gridWidth="1" gridHeight="1"
                    fill="2" ipadX="0" ipadY="0"
                    insetsTop="0" insetsLeft="0" insetsBottom="6" insetsRight="6"
                    anchor="17" weightX="0.0" weightY="0.0"/>
```
`fill`: `0`=NONE, `1`=BOTH, `2`=HORIZONTAL, `3`=VERTICAL
`anchor`: `10`=CENTER, `17`=EAST, `13`=WEST, `11`=NORTH, `15`=SOUTH

> **Important:** GridBagConstraints objects must be created in `setupComponents()`, not inside the GEN block.

---

## 22. Useful NetBeans MCP Tools

| Tool | When to use |
|------|-------------|
| `mcp5_getWorkspaceFolders` | Get open projects + paths |
| `mcp5_getOpenEditors` | See what's currently open |
| `mcp5_openFile` | Open a file in NetBeans editor |
| `mcp5_getDiagnostics` | Check for compile errors |
| `mcp5_saveDocument` | Save a file |
| `mcp5_getCurrentSelection` | Read selected text |
| `mcp5_show_markdown` | Show plan/summary in NetBeans |
| `mcp5_permission_prompt` | Show diff and ask Accept/Deny |
| `mcp5_add_maven_dependency` | Add a Maven dependency to pom.xml |
| `mcp5_add_library_jar` | Add a JAR to an Ant/NetBeans project lib/ |

---

## 23. Adding Libraries (MANDATORY — do this before writing any code that needs them)

### Maven project (has pom.xml)
Use `add_maven_dependency`. **Call it once per dependency, before writing Java code that imports it.**

```
add_maven_dependency(
  groupId    = "com.google.zxing",
  artifactId = "core",
  version    = "3.5.3"
)
```

- `projectPath` is optional — auto-detects first open Maven project.
- `scope` is optional — defaults to `compile`. Use `"test"` for test-only deps.
- **Idempotent**: safe to call even if you are unsure the dep already exists.

### Ant / classic NetBeans project (has nbproject/)
Use `add_library_jar`. **The JAR must already exist on disk.**

```
add_library_jar(
  jarPath     = "C:/path/to/library.jar",
  projectPath = "C:/Users/.../MyProject",   // optional, auto-detected
  copyToLib   = true                         // copies JAR into lib/, default true
)
```

- Updates `nbproject/project.properties` → `javac.classpath` automatically.
- After calling, instruct the user to do **Build → Clean and Build** in NetBeans.

---

## 24. Full-Application Generation Rules (ONE-PROMPT WORKFLOW)

When asked to build a complete application in a single prompt, follow this exact sequence:

### Step 1 — Discover project
```
mcp5_getWorkspaceFolders()   // get project path and type (Maven vs Ant)
```

### Step 2 — Add ALL required libraries first
Call `add_maven_dependency` or `add_library_jar` for **every** library the app will need **before writing a single line of Java code**. Do not skip this — missing dependencies cause compile errors that break the whole app.

### Step 3 — Write backend first (no UI yet)
Create in order:
1. Model classes (`model/`)
2. DAO / repository classes (`dao/`)
3. Service / business logic classes (`service/`)
4. Utility classes (`util/`)

### Step 4 — Write ALL UI forms
For each JFrame / JPanel / JDialog:
- Create **both** `ClassName.java` AND `ClassName.form` simultaneously
- `.java` and `.form` **must be 100% in sync** — every variable in `GEN-BEGIN:variables` must have a matching component in `.form` and vice-versa
- `setupComponents()` holds all non-GEN logic: table models, custom renderers, GridBagConstraints, etc.
- Never put `DefaultComboBoxModel`, `DefaultTableModel`, `GridBagConstraints`, or inner class instantiation inside the GEN block

### Step 5 — Entry point
Create `Main.java` with look-and-feel setup and `EventQueue.invokeLater`.

### Step 6 — Verify
```
mcp5_getDiagnostics()   // must return [] before declaring done
```
If errors exist, fix them. Do not stop until `getDiagnostics` returns empty.

### Non-negotiable rules for every UI file
- Every component declared in `GEN-BEGIN:variables` **must** be instantiated in `initComponents()`
- All types fully qualified: `javax.swing.JLabel`, not `JLabel`
- Event handler methods use `GEN-FIRST` / `GEN-LAST` on **standalone methods after** `initComponents()`, never inside anonymous listener bodies
- `.form` XML component names must exactly match Java variable names
- Colors in `.form` use `<Color red="hh" green="hh" blue="hh" type="rgb"/>` with **lowercase hex strings** — `ColorEditor.readFromXML` calls `Integer.parseInt(value, 16)` (radix 16); decimal integers like `red="255"` cause `IllegalArgumentException` (255 hex = 597 decimal, out of range)
