package com.jjkaps.epantry.ui.Fridge;



    public interface ItemTouchHelperAdapter {


        boolean onItemMove(int fromPosition, int toPosition);



        void onItemDismiss(int position);
    }
