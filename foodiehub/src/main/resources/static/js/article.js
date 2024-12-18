// 삭제 기능
const deleteButton = document.getElementById('delete-btn');

if(deleteButton){
    deleteButton.addEventListener('click',event=>{
        let id=document.getElementById('article-id').value;
        function success(){
            alert("삭제가 완료되었습니다.");
            location.replace("/articles")
        }

        function fail(){
            alert("삭제 실패했습니다.")
            location.replace("/articles")
        }

        httpRequest("DELETE","/api/articles/"+ id,null,success,fail);
    });
}

// 수정 기능
// 1) id가 modify-btn 인 엘리먼트 조회
const modifyButton = document.getElementById('modify-btn');

if(modifyButton){
    // 2) 클릭 이벤트가 감지되면 수정 API 요청
    modifyButton.addEventListener('click',event=>{
        let params= new URLSearchParams(location.search); // URL의 쿼리문자열 부분(?로 시작하는 부분) 반환
        let id =params.get('id');

        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });

        function success(){
            alert("수정 완료 되었습니다.")
            location.replace("/articles/"+id);
        }

        function fail(){
            alert("수정 실패했습니다.")
            location.replace("/articles/"+ id);
        }

        httpRequest("PUT","/api/articles/"+id,body,success,fail);
    });
}

// 등록 기능
// 1) id가 create-btn 인 엘리먼트
const createButton = document.getElementById("create-btn");
if(createButton){
    // 2) 클릭 이벤트가 감지되면 생성 API 요청
    createButton.addEventListener("click",(event)=>{
        body = JSON.stringify({
            title:document.getElementById("title").value,
            content:document.getElementById("content").value,
        });
        function success(){
            alert("등록 완료되었습니다.");
            location.replace("/articles");
        }
        function fail(){
            alert("등록 실패했습니다.");
            location.replace("/articles");
        }

        httpRequest("POST", "/api/articles", body, success,fail)
    });
}



// HTTP 요청을 보내는 함수
function httpRequest(method,url,body,success,fail){
    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지 에서 액세스 토큰 값을 가져와 헤더에 추가
            Authorization: "Bearer " + localStorage.getItem("access_token"),
            "Content-Type": "application/json",
        },
        body: body,
    }).then((response)=>{
        if(response.status===200 || response.status === 201){
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if(response.status === 401 && refresh_token){
            fetch("/api/token",{
                method:"POST",
                headers:{
                    Authorization :"Bearer " + localStorage.getItem("access_token"),
                    "Content-Type" : "application/json"
                },
                body :JSON.stringify({
                    refreshToken: getCookie("refresh_token"),
                }),
            })
                .then((res)=>{
                    if(res.ok){
                        return res.json();
                    }
                })
                .then((result)=>{
                    // 재발급이 성공하면 로컬 스토리지 값을 새로운 액세스 토큰 으로 교체
                    localStorage.setItem("access_token",result.accessToken);
                    httpRequest(method,url,body,success,fail);
                })
                .catch((error)=>fail());
        }else{
            return fail();
        }
    });
}

// 쿠키를 가져오는 함수
function getCookie(key)
{
    var result = null;
    var cookie = document.cookie.split(";");
    cookie.some(function(item){
        item = item.replace(" ","");
        var dic = item.split("=");
        if(key === dic[0]){
            result = dic[1];
            return true;
        }
    });
    return result;
}

// 로그아웃 기능
// const logoutButton = document.getElementById('logout-btn');
// if(logoutButton){
//     logoutButton.addEventListener('click',event=>{
//         function success(){
//             localStorage.removeItem('access_token'); // 로컬 스토리지에 저장된 액세스 토큰을 삭제
//             deleteCookie('refresh_token'); // 쿠키에 저장된 리프레시 토큰을 삭제
//             deleteCookie('oauth2_auth_request'); // 쿠키에 저장된 리프레시 토큰을 삭제
//             deleteCookie('JSESSIONID');
//             location.replace('/login');
//         }
//         function fail(){
//             alert('로그아웃 실패했습니다.')
//         }
//         httpRequest('DELETE','/api/refresh-token',null,success,fail);
//     })
// }

// const logoutButton = document.getElementById('logout-btn');
//
// if (logoutButton) {
//     logoutButton.addEventListener('click', () => {
//         // POST /logout 요청 (서버에서 세션 무효화 및 쿠키 삭제)
//         fetch('/logout', {
//             method: 'POST',
//             credentials: 'include', // 쿠키 포함
//         })
//             .then(response => {
//                 if (response.ok) {
//                     // 리프레시 토큰 삭제 요청
//                     return httpRequest('DELETE', '/api/refresh-token', null, () => {
//                         // 성공 시 클라이언트 저장 정보 삭제
//                         localStorage.removeItem('access_token');
//                         deleteCookie('refresh_token');
//                         deleteCookie('oauth2_auth_request');
//                         location.replace('/login'); // 로그인 페이지로 이동
//                     }, () => {
//                         alert('리프레시 토큰 삭제 실패');
//                     });
//                 } else {
//                     console.error('로그아웃 실패');
//                 }
//             })
//             .catch(error => console.error('Error during logout:', error));
//     });
// }


const logoutButton = document.getElementById('logout-btn');

if (logoutButton) {
    logoutButton.addEventListener('click', () => {
        fetch('/logout', { // 로그아웃 URL
            method: 'POST', // POST 요청
            credentials: 'include', // 쿠키 포함해서 서버로 전송
        })
            .then(response => {
                if (response.ok) {
                    console.log('로그아웃 성공');
                    localStorage.removeItem('access_token'); // 액세스 토큰 삭제
                    // deleteCookie('refresh_token');
                    deleteCookie('oauth2_auth_request');
                    location.replace('/login'); // 로그인 페이지로 리다이렉트
                } else {
                    console.error('로그아웃 실패');
                    alert('로그아웃에 실패했습니다.');
                }
            })
            .catch(error => {
                console.error('Error during logout:', error);
                alert('오류가 발생했습니다.');
            });
    });
} else {
    console.error('로그아웃 버튼을 찾을 수 없습니다.');
}



// 쿠키를 삭제하는 함수
function deleteCookie(name){
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}
