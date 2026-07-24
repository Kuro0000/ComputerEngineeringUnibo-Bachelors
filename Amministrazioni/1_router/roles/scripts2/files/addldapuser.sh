#!/bin/bash
USER="$1"
SLIB_LDAP_URI='ldap:///'


[[ ! "$USER" =~ ^[a-z]+$ || ! -z $(ldapsearch -x -H "$SLIB_LDAP_URI" \
        -b "dc=labammsis" -LLL "(uid=$USER)" dn 2>/dev/null) ]] && {
        echo "parametro deve essere solo composto da lettere minuscole e non deve esistere nel sistema ldap"
} || {

        while read -s -r -p "inserire una password " PASS 
        do
                if [[ -z "$PASS" ]]; then
                        exit 0
                elif [[ ${#PASS} -gt 9  ]]; then
                        break
                fi

        done
        echo "password inserita $PASS"
min=5000
current_max=$(ldapsearch -x -H "$SLIB_LDAP_URI" -b "dc=labammsis" -LLL "(objectClass=posixAccount)" uidNumber 2>/dev/null | awk '/^uidNumber:/ {print $2}' | sort -n | tail -n1)

# Pulisce da eventuali caratteri spuri
current_max=$(echo "$current_max" | tr -d -c '0-9')

if [[ -z "$current_max" || "$current_max" -lt "$min" ]]; then 
    max=$min
else 
    max=$(( current_max + 1 ))
fi
# 3. Creazione record LDAP con AUTENTICAZIONE AMMINISTRATORE
ldapadd -x -H "$SLIB_LDAP_URI" -D "cn=admin,dc=labammsis" -w "gennaio.marzo" <<EOF
dn: uid=$USER,ou=People,dc=labammsis
objectClass: top
objectClass: posixAccount
objectClass: shadowAccount
objectClass: inetOrgPerson
uid: $USER
cn: $USER
sn: $USER
homeDirectory: /home/$USER
loginShell: /bin/login.sh
uidNumber: $max
gidNumber: $max
description: sync
userPassword: {crypt}x
EOF

ldappasswd -x -H "ldap:///" -D "cn=admin,dc=labammsis" -w "gennaio.marzo" -s "$PASS" "uid=$USER,ou=People,dc=labammsis"

ssh 172.20.20.1 "mkdir -p /home/${USER}/.ssh"

   ssh 172.20.20.1 "chown -R ${USER}:{$USER} /home/${USER}N && chmod -R 700 /home/${USER}"
ssh 172.20.20.2 "mkdir -p /home/${USER}/.ssh"
   ssh 172.20.20.2 "chown -R ${USER}:{$USER} /home/${USER}N && chmod -R 700 /home/${USER}"

ssh-keygen -t rsa -b 2048 -f id_rsa -N "$PASS" -q

    cat id_rsa.pub | ssh 172.20.20.1 "cat > /home/${USER}/.ssh/authorized_keys"
    cat id_rsa.pub | ssh 172.20.20.2 "cat > /home/${USER}/.ssh/authorized_keys"
        cat id_rsa
        rm id_rsa*
}   

