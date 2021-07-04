# 1. app env vars configMap
kubectl apply -f configMap.yml
# 2. database credential secret 
kubectl create secret generic livesink-dw-credential --from-env-file=secret_file.txt
# 3. image pulling credential
kubectl create secret generic regcred --from-file=.dockerconfigjson=/root/.docker/config.json --type=kubernetes.io/dockerconfigjson
