package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;

public class MaterialIconDrawable extends Drawable {
    private String iconName;
    private int size;
    private int color;
    private Paint paint;
    private Typeface typeface;
    private Context context;
    
    public MaterialIconDrawable(Context context) {
        this.context = context;
        this.size = 24;
        this.color = Color.BLACK;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setTextAlign(Paint.Align.CENTER);
        loadFont();
    }
    
    private void loadFont() {
        try {
            // La fuente Material Symbols Outlined está en assets/fonts/
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/MaterialSymbolsOutlined-Regular.otf");
            paint.setTypeface(typeface);
        } catch (Exception e) {
            // Si no se puede cargar, intentar con el nombre alternativo
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/MaterialSymbolsOutlined[FILL,GRAD,opsz,wght].ttf");
                paint.setTypeface(typeface);
            } catch (Exception e2) {
                // Si tampoco funciona, usar la fuente antigua como fallback
                try {
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/MaterialIcons-Regular.ttf");
                    paint.setTypeface(typeface);
                    android.util.Log.w("MaterialIconDrawable", "Usando fuente Material Icons como fallback");
                } catch (Exception e3) {
                    // Si no se puede cargar ninguna fuente, usar la fuente del sistema
                    android.util.Log.e("MaterialIconDrawable", "Error al cargar fuente: " + e3.getMessage());
                    typeface = Typeface.DEFAULT;
                    paint.setTypeface(typeface);
                }
            }
        }
    }
    
    public void setIcon(String iconName) {
        this.iconName = iconName;
        invalidateSelf();
    }
    
    public void setSize(int size) {
        this.size = size;
        paint.setTextSize(size);
        invalidateSelf();
    }
    
    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
        invalidateSelf();
    }
    
    @Override
    public void draw(Canvas canvas) {
        if (iconName == null || iconName.isEmpty()) {
            return;
        }
        
        String unicode = MaterialSymbolsMapper.getUnicode(iconName);
        
        // Asegurar que la fuente esté configurada
        if (typeface != null) {
            paint.setTypeface(typeface);
        }
        
        paint.setColor(color);
        paint.setTextSize(size);
        
        int width = getBounds().width();
        int height = getBounds().height();
        
        if (width == 0 || height == 0) {
            // Si no hay bounds, usar el tamaño configurado
            width = size;
            height = size;
        }
        
        float x = width / 2f;
        float y = height / 2f - ((paint.descent() + paint.ascent()) / 2);
        
        canvas.drawText(unicode, x, y, paint);
    }
    
    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }
    
    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }
    
    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
    
    @Override
    public int getIntrinsicWidth() {
        return size;
    }
    
    @Override
    public int getIntrinsicHeight() {
        return size;
    }
    
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        // Asegurar que los bounds estén configurados correctamente
    }
}

