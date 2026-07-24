clear all; close all; clc

%%%%%%%%%%%%%%%% PARTE 1: CALCOLO DEL SISTEMA LINEARIZZATO %%%%%%%%%%%%%%%
%% Parametri del sistema
hR = 50;
AR = 0.07;
cR = 840.8;
cA = 1010;
mR = 2.542;
mA = 0.1041;
mAdot = 0.2;
Tin = 28;
k = 0.003;
TRe = 175;    % TR,e
Toute = 30.5036;   % Tout,e

%% Coppia di equilibrio
x_1e = TRe;
x_2e = Toute;
u_e  = -hR * AR * (x_2e - x_1e) * (1 + k * x_1e);
x_e  = [x_1e;x_2e];

%%%%%%%%%%%%%%%% PARTE 2: CALCOLO FUNZIONE DI TRASFERIMENTO %%%%%%%%%%%%%%%
A11 = -(hR*AR)/(mR*cR) - (u_e*k)/((1 + k*x_1e)^2 * mR * cR);
A12 = (hR*AR)/(mR*cR);
A21 = (hR*AR)/(mA*cA);
A22 = -mAdot/mA - hR*AR/(mA*cA);

B1 = 1/((1 + k*x_1e) * mR * cR);
B2 = 0;

A = [A11, A12; A21, A22];
B = [B1; B2];
C = [0, 1];
D = 0;

sys_ss = ss(A,B,C,D);
GG = tf(sys_ss);

figure(1);

bode(GG);
grid on, zoom on;
title('Diagramma di Bode della funzione di trasferimento G(s)');

igenvals = eig(A);
zeri_Gs = zero(GG);
poli_Gs = pole(GG);

%%%%%%%%%%%%%% PARTE 3: MAPPATURA SPECIFICHE DEL REGOLATORE %%%%%%%%%%%%%%%
% ampiezze gradini
WW = 4;
DD = 3.5;

% errore a regime
e_star = 0.002;

% attenuazione disturbo sull'uscita
A_d = 50;
omega_d_min = 1e-6;
omega_d_MAX = 0.4;

% attenuazione disturbo di misura
A_n = 60;
omega_n_min = 8e4;
omega_n_MAX = 9e6;

% Sovraelongazione massima e tempo d'assestamento all'1%
S_star = 11;
T_star = 0.01;

% Margine di fase
Mf_esp = 50;

% Calcolo specifiche S% => Margine di fase
xi_star = abs(log(S_star/100))/sqrt(pi^2 + log(S_star/100)^2);
Mf_min  = max(xi_star*100,Mf_esp);

%% Diagrammi di Bode di GG con specifiche
figure(2);
hold on;

% --- Zona proibita disturbo d(t) ---
Bnd_d_x = [omega_d_min; omega_d_MAX; omega_d_MAX; omega_d_min];
Bnd_d_y = [-500; -500; A_d; A_d];
patch(Bnd_d_x, Bnd_d_y,'r','FaceAlpha',0.2,'EdgeAlpha',0);

% --- Zona proibita rumore n(t) ---
Bnd_n_x = [omega_n_min; omega_n_MAX; omega_n_MAX; omega_n_min];
Bnd_n_y = [-A_n; -A_n; 100; 100];
patch(Bnd_n_x, Bnd_n_y,'g','FaceAlpha',0.2,'EdgeAlpha',0);

% --- Vincolo sul tempo d'assestamento ---
omega_Ta_min = 1e-4;
omega_Ta_max = 300/(Mf_min*T_star);

Bnd_Ta_x = [omega_Ta_min; omega_Ta_max; omega_Ta_max; omega_Ta_min];
Bnd_Ta_y = [0; 0; -500; -500];
patch(Bnd_Ta_x, Bnd_Ta_y,'y','FaceAlpha',0.2,'EdgeAlpha',0);

% --- Bode con margini ---
omega_plot_min = omega_d_min;
omega_plot_max = omega_n_MAX;
margin(GG,{omega_plot_min,omega_plot_max});
grid on; zoom on;
title('Funzione di trasferimento G(s) con specifiche');

% --- Vincolo sul margine di fase ---
omega_c_min = omega_Ta_max;
omega_c_max = omega_n_min;

phi_up = Mf_min - 180;
phi_low = -270; % lower bound per il plot

Bnd_Mf_x = [omega_c_min; omega_c_max; omega_c_max; omega_c_min];
Bnd_Mf_y = [phi_up; phi_up; phi_low; phi_low];
patch(Bnd_Mf_x, Bnd_Mf_y,'c','FaceAlpha',0.2,'EdgeAlpha',0);

% Recupero gli assi del Bode
ax = findall(gcf,'type','axes');
ax_mag = ax(2);     % asse del modulo
ax_phase = ax(1);   % asse della fase

%% --- LEGENDA PER IL MODULO ---
axes(ax_mag);

% placeholder invisibili per la legend
hG = plot(nan,nan,'b','LineWidth',1.5);            % curva G(s)
hD = patch(nan,nan,'r','FaceAlpha',0.2,'EdgeAlpha',0); % area d(t)
hN = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0); % area n(t)
hWc = patch(nan,nan,'y','FaceAlpha',0.2,'EdgeAlpha',0); % area ωc

legend([hG hD hN hWc], ...
       {'G(s)', 'zona proibita Ad', 'Zona proibita An', '\omega_c'}, ...
       'Location','southwest');

%% --- LEGENDA PER LA FASE ---
axes(ax_phase);

hMf = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0);

legend(hMf, {'Margine di fase'}, 'Location','southwest');

%%%%%%%%%%%%%%%%% PARTE 4: SINTESI DEL REGOLATORE STATICO %%%%%%%%%%%%%%%%%
% valore minimo prescritto per L(0)
mu_s_error = (DD+WW)/e_star;
mu_s_dist  = 10^(A_d/20);

% guadagno minimo del regolatore ottenuto come L(0)/G(0)
G_0 = abs(evalfr(GG,0));
G_omega_d_MAX = abs(evalfr(GG,1i*omega_d_MAX));

RR_s = max(mu_s_error/G_0,mu_s_dist/G_omega_d_MAX);

% Sistema esteso
GG_e = RR_s*GG;

%% Diagrammi di Bode di Ge con specifiche
figure(3);
hold on;

% --- Zona proibita disturbo d(t) ---
patch(Bnd_d_x, Bnd_d_y,'r','FaceAlpha',0.2,'EdgeAlpha',0);

% --- Zona proibita rumore n(t) ---
patch(Bnd_n_x, Bnd_n_y,'g','FaceAlpha',0.2,'EdgeAlpha',0);

patch(Bnd_Ta_x, Bnd_Ta_y,'y','FaceAlpha',0.2,'EdgeAlpha',0);

% --- Bode con margini ---
margin(GG_e,{omega_plot_min,omega_plot_max});
grid on; zoom on;
title('Funzione di trasferimento Ge(s) con il regolatore statico');

% --- Vincolo sul margine di fase ---
omega_c_min = omega_Ta_max;
omega_c_max = omega_n_min;

Bnd_Mf_x = [omega_c_min; omega_c_max; omega_c_max; omega_c_min];
Bnd_Mf_y = [phi_up; phi_up; phi_low; phi_low];
patch(Bnd_Mf_x, Bnd_Mf_y,'c','FaceAlpha',0.2,'EdgeAlpha',0);

% Recupero gli assi del Bode
ax = findall(gcf,'type','axes');
ax_mag = ax(2);     % asse del modulo
ax_phase = ax(1);   % asse della fase

%% --- LEGENDA PER IL MODULO ---
axes(ax_mag);

% placeholder invisibili per la legenda
hG = plot(nan,nan,'b','LineWidth',1.5);            % curva GG_e(s)

hLL = plot(nan,nan,'r','LineWidth',1.5);            % curva LL(s)
hD = patch(nan,nan,'r','FaceAlpha',0.2,'EdgeAlpha',0); % area d(t)
hN = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0); % area n(t)
hWc = patch(nan,nan,'y','FaceAlpha',0.2,'EdgeAlpha',0); % area ωc

legend([hG hLL hD hN hWc], ...
       {'Ge(s)', 'LL(s)', 'zona proibita Ad', 'Zona proibita An', '\omega_c'}, ...
       'Location','southwest');

%% --- LEGENDA PER LA FASE ---
axes(ax_phase);

hMf = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0);

legend(hMf, {'Margine di fase'}, 'Location','southwest');


%%%%%%%%%%%%%%%%% PARTE 5: SINTESI DEL REGOLATORE DINAMICO %%%%%%%%%%%%%%%%
Mf_star = Mf_min+5;
omega_c_star = 550; % scelto da noi

mag_omega_c_star = abs(evalfr(GG_e,1i*omega_c_star));
arg_omega_c_star    = rad2deg(angle(evalfr(GG_e,1i*omega_c_star)));

M_star = 1/mag_omega_c_star;
phi_star = Mf_star - 180 - arg_omega_c_star;

%%%%%%%%% Verifica Valori %%%%%%%%%
% Verifico che: M_star > 1
condition1 = ['Verifica della condizione M_star > 1: ', num2str(M_star)];
disp(condition1);

% Verifico che 0 < phi_star < π/2
condition2 = ['Verifica della condizione 0 < phi_star < π/2: 0 < ', num2str((phi_star * pi)/180), ' < π/2'];
disp(condition2);

% Verifico che cos(phi_star) > 1/M_star
condition3 = ['Verifica della condizione cos(phi_star) > 1/M_star: ', num2str(cos((phi_star * pi)/180)), ' > ', num2str(1/M_star)];
disp(condition3);

% FORMULE DI INVERSIONE
tau = (M_star - cos(phi_star*pi/180))/(omega_c_star*sin(phi_star*pi/180));
alpha_tau = (cos(phi_star*pi/180) - 1/M_star)/(omega_c_star*sin(phi_star*pi/180));
alpha = alpha_tau / tau;

if min(tau,alpha) < 0
    fprintf('Errore: parametri rete anticipatrice negativi');
    return;
end

s  = tf('s');
RR_d = (1 + tau*s)/(1 + alpha * tau*s);

RR = RR_s*RR_d;

LL = RR*GG; % funzione di anello

figure(4);
hold on;

% Specifiche su d
patch(Bnd_d_x, Bnd_d_y,'r','FaceAlpha',0.2,'EdgeAlpha',0);
hold on;

% Specifiche su n
patch(Bnd_n_x, Bnd_n_y,'g','FaceAlpha',0.2,'EdgeAlpha',0);
hold on;

% Specifiche tempo d'assestamento
omega_Ta_min = 1e-4;
omega_Ta_max = omega_c_min;
patch(Bnd_Ta_x, Bnd_Ta_y,'y','FaceAlpha',0.2,'EdgeAlpha',0);
hold on;

% --- BODE ---
margin(GG_e,{omega_plot_min,omega_plot_max});
grid on; zoom on;
title('Paragone con funzione di trasferimento Ge(s) e L(s)');

omega_c_min = omega_Ta_max;
omega_c_max = omega_n_min;

Bnd_Mf_x = [omega_c_min; omega_c_max; omega_c_max; omega_c_min];
Bnd_Mf_y = [phi_up; phi_up; phi_low; phi_low];
patch(Bnd_Mf_x, Bnd_Mf_y,'g','FaceAlpha',0.2,'EdgeAlpha',0);
% Recupero gli assi del Bode
ax = findall(gcf,'type','axes');
ax_mag = ax(2);     % asse del modulo
ax_phase = ax(1);   % asse della fase

%% --- LEGENDA PER IL MODULO ---
axes(ax_mag);

% placeholder invisibili per la legenda
hG = plot(nan,nan,'b','LineWidth',1.5);            % curva G(s)

hLL = plot(nan,nan,'r','LineWidth',1.5);            % curva LL(s)
hD = patch(nan,nan,'r','FaceAlpha',0.2,'EdgeAlpha',0); % area d(t)
hN = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0); % area n(t)
hWc = patch(nan,nan,'y','FaceAlpha',0.2,'EdgeAlpha',0); % area ωc (blu)

legend([hG hLL hD hN hWc], ...
       {'Ge(s)', 'LL(s)', 'zona proibita Ad', 'Zona proibita An', '\omega_c'}, ...
       'Location','southwest');

%% --- LEGENDA PER LA FASE ---
axes(ax_phase);

hMf = patch(nan,nan,'g','FaceAlpha',0.2,'EdgeAlpha',0);

legend(hMf, {'Margine di fase'}, 'Location','southwest');

margin(LL,{omega_plot_min,omega_plot_max});


% STOP qui per sistema con controllore dinamico + specifiche
if 0
    return;
end

%%%%%%%%%%%%%%%%%% PARTE 6: TEST SUL SISTEMA LINEARIZZATO% %%%%%%%%%%%%%%%%
% Funzione di sensitività complementare
FF = LL/(1+LL);

% Risposta al gradino
figure(5);

T_simulation = 2*T_star;
[y_step,t_step] = step(WW*FF, T_simulation);
plot(t_step,y_step,'b');
grid on, zoom on, hold on;
title('Risposta del sistema a un gradino');

LV = evalfr(WW*FF,0);

% vincolo sovraelongazione
patch([0,T_simulation,T_simulation,0],[LV*(1+S_star/100),LV*(1+S_star/100),LV*2,LV*2],'r','FaceAlpha',0.3,'EdgeAlpha',0.5);

% vincolo tempo di assestamento all'5%
patch([T_star,T_simulation,T_simulation,T_star],[LV*(1-0.05),LV*(1-0.05),0,0],'g','FaceAlpha',0.1,'EdgeAlpha',0.5);
patch([T_star,T_simulation,T_simulation,T_star],[LV*(1+0.05),LV*(1+0.05),LV*2,LV*2],'g','FaceAlpha',0.1,'EdgeAlpha',0.1);

ylim([0,LV*2]);

Legend_step = ["Risposta al gradino"; "Vincolo sovraelongazione"; "Vincolo tempo di assestamento"];
legend(Legend_step);

%% Check disturbo in uscita

% Funzione di sensitività
SS = 1/(1+LL);
figure(6);

% Simulazione disturbo in uscita
DD = 1.5;
tt = 0:1e-5:5e2;
risultato_sommatoria = 0;

% Sommatoria da kk=1 a kk=4
for kk = 1:4
    termine = sin(0.08 * kk * tt);
    risultato_sommatoria = risultato_sommatoria + termine;
end

dd = DD * risultato_sommatoria;
y_d = lsim(SS,dd,tt);
hold on, grid on, zoom on
plot(tt,dd,'m')
plot(tt,y_d,'b')
grid on
title('Disturbo in uscita');
legend('d(t)','y_d(t)')

%% Check disturbo di misura

% Funzione di sensitività complementare
FF = LL/(1+LL);
figure(7);

% Simulazione disturbo di misura
NN = 3;
tt = 0:1e-5:2*1e-3;
risultato_sommatoria = 0;
% Sommatoria da k=1 a k=4
for kk = 1:4
    termine = sin(5e4 * kk * tt);
    risultato_sommatoria = risultato_sommatoria + termine;
end
nn = NN*risultato_sommatoria;
y_n = lsim(-FF,nn,tt);
hold on, grid on, zoom on
plot(tt,nn,'m')
plot(tt,y_n,'b')
grid on
title('Disturbo di misura');
legend('n(t)','y_n(t')

%%%%%%%%%%%%%%%% ANIMAZIONE SCALDATORE %%%%%%%%%%%%%%%
disp("Avvio animazione dello scaldatore con controllore...");

%% 1. Preparazione simulazione
x0 = [0.5; 0];
f = 1/5;            % Frequenza dell'ingresso (Hz)
tspan = 0:0.05:10;  % Tempo di simulazione

% SELEZIONE TIPO DI INGRESSO
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% OPZIONE 1: Onda quadra (default)
%uu = WW*square(2*pi*f*tspan, 50);
% OPZIONE 2: Sinusoide
 uu = WW*sin(2*pi*f*tspan);
% OPZIONE 3: Gradino
% uu = WW*ones(size(tspan));
% OPZIONE 4: Rampa
% uu = WW*tspan/max(tspan);


%% 2. Simulazione
[YY, TT, ~] = lsim(FF, uu, tspan, x0);

% Calcolo temperature assolute
Tout_abs = YY + Toute;
Tref_abs = uu' + Toute;

% Range temperature per il termometro
T_min = min(Tout_abs) - 2;
T_max = max(Tout_abs) + 2;
delta_T = T_max - T_min;

%% 3. Impostazione della figura
figanim2 = figure(8);
clf(figanim2); % Pulisce la figura se esiste già
set(figanim2, 'Position', [100, 100, 900, 600]);

% --- SUBPLOT 1: Grafico temporale ---
subplot(2,1,1);
h1 = plot(NaN, NaN, 'b--', 'LineWidth', 2); % Linea Riferimento
hold on;
h2 = plot(NaN, NaN, 'r', 'LineWidth', 2);   % Linea Uscita
xlabel('Tempo [s]', 'FontSize', 11);
ylabel('Variazione temperatura [°C]', 'FontSize', 11);
legend('Riferimento', 'T_{out} misurata', 'Location', 'Best');
title('Evoluzione del sistema controllato', 'FontSize', 13, 'FontWeight', 'bold');
xlim([0, max(TT)]);
ylim([min([uu, YY']) - 0.5, max([uu, YY']) + 0.5]);
grid on;

% --- SUBPLOT 2: Termometro ---
subplot(2,1,2);
hold on;
axis equal;
axis off;
xlim([0, 10]);
ylim([0, 10]);

% Parametri geometrici
bulb_x = 5;
bulb_y = 1.5;
bulb_r = 0.6;
tube_width = 0.3;
tube_height = 6;
tube_x = bulb_x - tube_width/2;
tube_y = bulb_y + bulb_r;

% Disegno struttura statica
rectangle('Position', [bulb_x-bulb_r, bulb_y-bulb_r, 2*bulb_r, 2*bulb_r], ...
          'Curvature', [1,1], 'FaceColor', [0.9 0.9 0.9], 'EdgeColor', 'k', 'LineWidth', 2);
% Tubo esterno
rectangle('Position', [tube_x, tube_y, tube_width, tube_height], ...
          'FaceColor', [0.95 0.95 0.95], 'EdgeColor', 'k', 'LineWidth', 2);

% Oggetti dinamici (Mercurio e Indicatori)
% Mercurio nel bulbo
h_mercury_bulb = rectangle('Position', [bulb_x-bulb_r*0.8, bulb_y-bulb_r*0.8, 2*bulb_r*0.8, 2*bulb_r*0.8], ...
                           'Curvature', [1,1], 'FaceColor', [1 0 0], 'EdgeColor', 'none');
% Mercurio nel tubo (Altezza iniziale 0)
h_mercury = rectangle('Position', [tube_x+0.05, tube_y, tube_width-0.1, 0], ...
                      'FaceColor', [1 0 0], 'EdgeColor', 'none');

% Etichette
text(bulb_x, 0.3, 'SCALDATORE', 'FontSize', 12, 'FontWeight', 'bold', 'HorizontalAlignment', 'center');
title('Termometro - Temperatura Uscita Riscaldatore', 'FontSize', 13, 'FontWeight', 'bold');

% Indicatore riferimento (freccia e testo)
h_ref_arrow = plot(NaN, NaN, '>b', 'MarkerSize', 15, 'MarkerFaceColor', 'b');
h_ref_text = text(NaN, NaN, 'Rif', 'FontSize', 8, 'Color', 'b', 'FontWeight', 'bold');

%% 4. Animazione
disp("Animazione in corso...");

for i = 1:length(TT)
    if ~isvalid(figanim2)
        break; % Interrompe se chiudi la finestra
    end

    % --- Aggiornamento Grafico Temporale ---
    set(h1, 'XData', TT(1:i), 'YData', uu(1:i));
    set(h2, 'XData', TT(1:i), 'YData', YY(1:i));

    % --- Calcoli per Termometro ---
    T_current = Tout_abs(i);
    T_ref_current = Tref_abs(i);

    % Normalizzazione (0-1) usando delta_T precalcolato
    T_norm = (T_current - T_min) / delta_T;

    % Calcolo altezza mercurio (con Clamp tra 0 e tube_height)
    mercury_height = max(0, min(T_norm * tube_height, tube_height));

    % Aggiorna geometria mercurio
    set(h_mercury, 'Position', [tube_x+0.05, tube_y, tube_width-0.1, mercury_height]);

    % Cambio colore dinamico (da Arancione a Rosso)
    % Clamp del colore tra 0 e 1 per evitare errori se T esce dal grafico
    color_val = max(0, min(1, T_norm));
    mercury_color = [1, 0.3*(1-color_val), 0];

    set(h_mercury, 'FaceColor', mercury_color);
    set(h_mercury_bulb, 'FaceColor', mercury_color);

    % --- Aggiornamento Freccia Riferimento ---
    ref_norm = (T_ref_current - T_min) / delta_T;
    ref_y = tube_y + ref_norm * tube_height;
    % Clamp posizione freccia per restare visivamente nel tubo
    ref_y = max(tube_y, min(ref_y, tube_y + tube_height));

    set(h_ref_arrow, 'XData', tube_x - 0.4, 'YData', ref_y);
    set(h_ref_text, 'Position', [tube_x - 0.8, ref_y]);

    pause(0.05);
    drawnow;
end
disp("Animazione terminata.");
