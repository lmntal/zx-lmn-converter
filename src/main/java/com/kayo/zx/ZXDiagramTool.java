package com.kayo.zx;

import javax.swing.SwingUtilities;

import com.kayo.zx.controller.AppController;

public class ZXDiagramTool {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      AppController controller = new AppController();
      controller.showFrame();
    });
  }
}
