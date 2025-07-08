       IDENTIFICATION DIVISION.
       PROGRAM-ID. TAX-CALC.
       AUTHOR. LEGACY-SYSTEMS-TEAM.
      *****************************************************************
      * LEGACY TAX CALCULATION SYSTEM - WRITTEN IN 1985             *
      * CALCULATES FEDERAL AND STATE TAXES FOR INDIVIDUAL RETURNS   *
      * HANDLES STANDARD DEDUCTION, ITEMIZED DEDUCTIONS, TAX RATES  *
      * CRITICAL: USED FOR MILLIONS OF TAX RETURNS ANNUALLY         *
      *****************************************************************
       
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TAX-INPUT-FILE ASSIGN TO "TAXIN.DAT"
               ORGANIZATION IS SEQUENTIAL.
           SELECT TAX-OUTPUT-FILE ASSIGN TO "TAXOUT.DAT"
               ORGANIZATION IS SEQUENTIAL.
       
       DATA DIVISION.
       FILE SECTION.
       FD TAX-INPUT-FILE.
       01 TAX-INPUT-RECORD.
           05 TAXPAYER-ID         PIC X(9).
           05 FILING-STATUS       PIC X(1).
              88 SINGLE           VALUE "S".
              88 MARRIED-JOINT    VALUE "M".
              88 HEAD-HOUSEHOLD   VALUE "H".
           05 GROSS-INCOME        PIC 9(8)V99.
           05 ITEMIZED-DEDUCTIONS PIC 9(7)V99.
           05 STATE-CODE          PIC X(2).
       
       FD TAX-OUTPUT-FILE.
       01 TAX-OUTPUT-RECORD.
           05 OUT-TAXPAYER-ID     PIC X(9).
           05 OUT-FEDERAL-TAX     PIC 9(7)V99.
           05 OUT-STATE-TAX       PIC 9(7)V99.
           05 OUT-TOTAL-TAX       PIC 9(8)V99.
           05 OUT-EFFECTIVE-RATE  PIC 99V99.
       
       WORKING-STORAGE SECTION.
       01 WS-FLAGS.
           05 WS-EOF-FLAG         PIC X VALUE "N".
              88 END-OF-FILE      VALUE "Y".
       
       01 WS-TAX-CALCULATIONS.
           05 WS-ADJUSTED-INCOME  PIC 9(8)V99.
           05 WS-TAXABLE-INCOME   PIC 9(8)V99.
           05 WS-FEDERAL-TAX      PIC 9(7)V99.
           05 WS-STATE-TAX        PIC 9(7)V99.
           05 WS-TOTAL-TAX        PIC 9(8)V99.
           05 WS-STANDARD-DED     PIC 9(6)V99.
           05 WS-STATE-RATE       PIC 99V9999.
       
       01 WS-TAX-BRACKETS.
           05 WS-BRACKET-1        PIC 9(6) VALUE 10275.
           05 WS-BRACKET-2        PIC 9(6) VALUE 41775.
           05 WS-BRACKET-3        PIC 9(6) VALUE 89450.
           05 WS-RATE-1           PIC 99V99 VALUE 10.00.
           05 WS-RATE-2           PIC 99V99 VALUE 12.00.
           05 WS-RATE-3           PIC 99V99 VALUE 22.00.
           05 WS-RATE-4           PIC 99V99 VALUE 24.00.
       
       PROCEDURE DIVISION.
       MAIN-PROCESSING.
           PERFORM INITIALIZE-PROGRAM
           PERFORM PROCESS-TAX-RECORDS UNTIL END-OF-FILE
           PERFORM CLEANUP-PROGRAM
           STOP RUN.
       
       INITIALIZE-PROGRAM.
           OPEN INPUT TAX-INPUT-FILE
           OPEN OUTPUT TAX-OUTPUT-FILE
           READ TAX-INPUT-FILE
               AT END MOVE "Y" TO WS-EOF-FLAG
           END-READ.
       
       PROCESS-TAX-RECORDS.
           PERFORM CALCULATE-STANDARD-DEDUCTION
           PERFORM CALCULATE-TAXABLE-INCOME
           PERFORM CALCULATE-FEDERAL-TAX
           PERFORM CALCULATE-STATE-TAX
           PERFORM CALCULATE-TOTALS
           PERFORM WRITE-OUTPUT-RECORD
           READ TAX-INPUT-FILE
               AT END MOVE "Y" TO WS-EOF-FLAG
           END-READ.
       
       CALCULATE-STANDARD-DEDUCTION.
           EVALUATE TRUE
               WHEN SINGLE
                   MOVE 13850 TO WS-STANDARD-DED
               WHEN MARRIED-JOINT
                   MOVE 27700 TO WS-STANDARD-DED
               WHEN HEAD-HOUSEHOLD
                   MOVE 20800 TO WS-STANDARD-DED
               WHEN OTHER
                   MOVE 13850 TO WS-STANDARD-DED
           END-EVALUATE.
       
       CALCULATE-TAXABLE-INCOME.
           IF ITEMIZED-DEDUCTIONS > WS-STANDARD-DED
               SUBTRACT ITEMIZED-DEDUCTIONS FROM GROSS-INCOME
                   GIVING WS-TAXABLE-INCOME
           ELSE
               SUBTRACT WS-STANDARD-DED FROM GROSS-INCOME
                   GIVING WS-TAXABLE-INCOME
           END-IF
           
           IF WS-TAXABLE-INCOME < ZERO
               MOVE ZERO TO WS-TAXABLE-INCOME
           END-IF.
       
       CALCULATE-FEDERAL-TAX.
           MOVE ZERO TO WS-FEDERAL-TAX
           
           IF WS-TAXABLE-INCOME > WS-BRACKET-1
               COMPUTE WS-FEDERAL-TAX = WS-BRACKET-1 * (WS-RATE-1 / 100)
               
               IF WS-TAXABLE-INCOME > WS-BRACKET-2
                   COMPUTE WS-FEDERAL-TAX = WS-FEDERAL-TAX +
                       ((WS-BRACKET-2 - WS-BRACKET-1) * (WS-RATE-2 / 100))
                   
                   IF WS-TAXABLE-INCOME > WS-BRACKET-3
                       COMPUTE WS-FEDERAL-TAX = WS-FEDERAL-TAX +
                           ((WS-BRACKET-3 - WS-BRACKET-2) * (WS-RATE-3 / 100)) +
                           ((WS-TAXABLE-INCOME - WS-BRACKET-3) * (WS-RATE-4 / 100))
                   ELSE
                       COMPUTE WS-FEDERAL-TAX = WS-FEDERAL-TAX +
                           ((WS-TAXABLE-INCOME - WS-BRACKET-2) * (WS-RATE-3 / 100))
                   END-IF
               ELSE
                   COMPUTE WS-FEDERAL-TAX = WS-FEDERAL-TAX +
                       ((WS-TAXABLE-INCOME - WS-BRACKET-1) * (WS-RATE-2 / 100))
               END-IF
           ELSE
               COMPUTE WS-FEDERAL-TAX = WS-TAXABLE-INCOME * (WS-RATE-1 / 100)
           END-IF.
       
       CALCULATE-STATE-TAX.
      * SIMPLIFIED STATE TAX CALCULATION
           EVALUATE STATE-CODE
               WHEN "CA"
                   MOVE 0.0925 TO WS-STATE-RATE
               WHEN "TX"
                   MOVE 0.0000 TO WS-STATE-RATE
               WHEN "NY"
                   MOVE 0.0685 TO WS-STATE-RATE
               WHEN "FL"
                   MOVE 0.0000 TO WS-STATE-RATE
               WHEN OTHER
                   MOVE 0.0500 TO WS-STATE-RATE
           END-EVALUATE
           
           COMPUTE WS-STATE-TAX = WS-TAXABLE-INCOME * WS-STATE-RATE.
       
       CALCULATE-TOTALS.
           COMPUTE WS-TOTAL-TAX = WS-FEDERAL-TAX + WS-STATE-TAX
           MOVE WS-FEDERAL-TAX TO OUT-FEDERAL-TAX
           MOVE WS-STATE-TAX TO OUT-STATE-TAX
           MOVE WS-TOTAL-TAX TO OUT-TOTAL-TAX
           
           IF GROSS-INCOME > ZERO
               COMPUTE OUT-EFFECTIVE-RATE = 
                   (WS-TOTAL-TAX / GROSS-INCOME) * 100
           ELSE
               MOVE ZERO TO OUT-EFFECTIVE-RATE
           END-IF.
       
       WRITE-OUTPUT-RECORD.
           MOVE TAXPAYER-ID TO OUT-TAXPAYER-ID
           WRITE TAX-OUTPUT-RECORD.
       
       CLEANUP-PROGRAM.
           CLOSE TAX-INPUT-FILE
           CLOSE TAX-OUTPUT-FILE.