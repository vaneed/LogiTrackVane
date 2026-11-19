// JavaScript sencillo: fetch llama al backend y el token vive solo en memoria.
let token = '';
let usuario = null;
let registro = false;
let bodegas = [];
let productos = [];
let temporizador;
const $ = id => document.getElementById(id);
const dinero = valor => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(valor);
// Escapamos datos del servidor antes de mostrarlos en HTML.
function texto(valor) {
    return String(valor ?? '—').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}
function avisar(mensaje, error = false) {
    clearTimeout(temporizador);
    $('mensaje').textContent = mensaje;
    $('mensaje').classList.toggle('error', error);
    $('mensaje').hidden = false;
    temporizador = setTimeout(() => $('mensaje').hidden = true, 8000);
}
async function api(ruta, metodo = 'GET', datos) {
    const headers = {};
    if (token) headers.Authorization = 'Bearer ' + token;
    if (datos) headers['Content-Type'] = 'application/json';
    const respuesta = await fetch(ruta, { method: metodo, headers, body: datos ? JSON.stringify(datos) : undefined });
    if (respuesta.status === 204) return null;
    const resultado = await respuesta.json();
    if (!respuesta.ok) {
        if (respuesta.status === 401 && token) cerrarSesion();
        throw new Error(resultado.mensaje || 'No se pudo completar la solicitud.');
    }
    return resultado;
}
// Este envoltorio muestra errores sin dejarlos perdidos en la consola.
function tarea(accion) { return async evento => { try { await accion(evento); } catch (e) { avisar(e.message, true); } }; }
function tabla(titulos, filas) {
    if (!filas.length) return '<p class="vacio">Todavía no hay registros para mostrar ♡</p>';
    return '<table><thead><tr>' + titulos.map(t => '<th>' + t + '</th>').join('') + '</tr></thead><tbody>' + filas.map(f => '<tr>' + f.map(c => '<td>' + c + '</td>').join('') + '</tr>').join('') + '</tbody></table>';
}
function nombreBodega(id) { return texto(bodegas.find(b => b.id === id)?.nombre ?? id); }
function nombreProducto(id) { return texto(productos.find(p => p.id === id)?.nombre ?? id); }
function admin() { return usuario?.rol === 'ADMIN'; }
async function catalogos() { [bodegas, productos] = await Promise.all([api('/bodegas'), api('/productos')]); }
async function ver(vista) {
    document.querySelectorAll('.vista').forEach(s => s.hidden = s.id !== vista);
    document.querySelectorAll('[data-vista]').forEach(b => b.classList.toggle('activo', b.dataset.vista === vista));
    await catalogos();
    if (vista === 'resumen') await cargarResumen();
    if (vista === 'bodegas') cargarBodegas();
    if (vista === 'productos') await cargarProductos();
    if (vista === 'movimientos') await cargarMovimientos();
    if (vista === 'auditorias') await cargarAuditorias();
}
document.querySelectorAll('[data-vista]').forEach(b => b.onclick = tarea(() => ver(b.dataset.vista)));
document.querySelectorAll('[data-cancelar]').forEach(b => b.onclick = () => $(b.dataset.cancelar).hidden = true);
$('cambiarAcceso').onclick = () => {
    registro = !registro;
    $('tituloAcceso').textContent = registro ? 'Tu espacio empieza aquí ♡' : 'Qué lindo verte por aquí ♡';
    $('textoAcceso').textContent = registro ? 'Usa un usuario de 3 a 50 letras, números o guion bajo y una contraseña de al menos 8 caracteres.' : 'Inicia sesión para consultar tu inventario.';
    $('botonAcceso').textContent = registro ? 'Crear mi cuenta →' : 'Entrar a mi inventario →';
    $('cambiarAcceso').textContent = registro ? 'Ya tengo una cuenta' : '¿Primera vez? Crear una cuenta';
    $('formAcceso').elements.clave.minLength = registro ? 8 : 1;
    $('formAcceso').elements.clave.autocomplete = registro ? 'new-password' : 'current-password';
};
$('formAcceso').onsubmit = tarea(async e => {
    e.preventDefault();
    $('botonAcceso').disabled = true;
    try {
        usuario = await api(registro ? '/auth/register' : '/auth/login', 'POST', Object.fromEntries(new FormData(e.target)));
        token = usuario.token;
        $('formAcceso').reset();
        $('acceso').hidden = true; $('panel').hidden = false; $('salir').hidden = false;
        $('usuarioActual').textContent = usuario.nombre + ' · ' + usuario.rol;
        document.querySelectorAll('[data-admin]').forEach(el => el.hidden = !admin());
        await ver('resumen');
    } finally { $('botonAcceso').disabled = false; }
});
function cerrarSesion() {
    token = ''; usuario = null; bodegas = []; productos = [];
    $('acceso').hidden = false; $('panel').hidden = true; $('salir').hidden = true; $('usuarioActual').textContent = '';
    ['formBodega','formProducto','formMovimiento'].forEach(id => { $(id).reset(); $(id).hidden = true; });
    $('detalleBodega').hidden = true;
    $('filtroMovimientos').reset(); $('filtroAuditorias').reset(); $('stockBajo').checked = false;
}
$('salir').onclick = cerrarSesion;
async function cargarResumen() {
    const datos = await api('/reportes/resumen');
    const total = datos.stockPorBodega.reduce((s, b) => s + b.stockTotal, 0);
    const bajos = productos.filter(p => p.stock < 10).length;
    $('indicadores').innerHTML = [['Bodegas activas', bodegas.length, 'Espacios para organizar'], ['Unidades en inventario', total, 'Stock total disponible'], ['Productos con stock bajo', bajos, 'Menos de 10 unidades']].map(d => '<div class="indicador"><span>' + d[0] + '</span><strong>' + d[1] + '</strong><small>' + d[2] + '</small></div>').join('');
    const filas = (lista, valor) => lista.length ? lista.map(d => '<div class="fila-resumen"><span>' + texto(d.nombre) + '</span><span class="cantidad">' + d[valor] + ' uds.</span></div>').join('') : '<p class="vacio">Aún no hay movimientos ♡</p>';
    $('stockBodegas').innerHTML = filas(datos.stockPorBodega, 'stockTotal');
    $('masMovidos').innerHTML = filas(datos.productosMasMovidos, 'cantidadMovida');
}
function acciones(tipo, id) { return admin() ? '<button class="secundario" data-editar="' + tipo + '" data-id="' + id + '">Editar</button><button class="secundario" data-eliminar="' + tipo + '" data-id="' + id + '">Eliminar</button>' : ''; }
function cargarBodegas() {
    $('listaBodegas').innerHTML = tabla(['ID / Bodega','Ubicación','Capacidad','Encargado','Acciones'], bodegas.map(b => [texto(b.id) + ' · ' + texto(b.nombre), texto(b.ubicacion), b.capacidad + ' uds.', texto(b.encargado), '<button class="secundario" data-existencias="' + b.id + '">Ver stock</button>' + acciones('Bodega', b.id)]));
}
async function cargarProductos() {
    const lista = $('stockBajo').checked ? await api('/productos/stock-bajo') : productos;
    $('listaProductos').innerHTML = tabla(['ID / Producto','Categoría','Stock','Precio','Acciones'], lista.map(p => [texto(p.id) + ' · ' + texto(p.nombre), texto(p.categoria), '<span class="badge ' + (p.stock < 10 ? 'bajo' : '') + '">' + p.stock + ' uds.</span>', dinero(p.precio), acciones('Producto', p.id) || 'Solo consulta']));
}
$('stockBajo').onchange = tarea(cargarProductos);
function abrirFormulario(tipo, dato) {
    const form = $('form' + tipo); form.reset(); form.hidden = false;
    $('titulo' + tipo).textContent = (dato ? 'Editar ' : 'Registrar ') + tipo.toLowerCase();
    if (dato) Object.keys(dato).forEach(k => { if (form.elements[k]) form.elements[k].value = dato[k]; });
}
$('nuevaBodega').onclick = () => abrirFormulario('Bodega');
$('nuevoProducto').onclick = () => abrirFormulario('Producto');
for (const tipo of ['Bodega', 'Producto']) {
    $('form' + tipo).onsubmit = tarea(async e => {
        e.preventDefault();
        const datos = Object.fromEntries(new FormData(e.target)); const id = datos.id; delete datos.id;
        if (tipo === 'Bodega') datos.capacidad = Number(datos.capacidad); else datos.precio = Number(datos.precio);
        const ruta = tipo === 'Bodega' ? '/bodegas' : '/productos';
        await api(ruta + (id ? '/' + id : ''), id ? 'PUT' : 'POST', datos);
        e.target.hidden = true;
        await ver(ruta.slice(1)); avisar('Cambios guardados correctamente ♡');
    });
}
document.addEventListener('click', tarea(async e => {
    const boton = e.target.closest('button'); if (!boton) return;
    const id = Number(boton.dataset.id);
    if (boton.dataset.editar) {
        const tipo = boton.dataset.editar;
        abrirFormulario(tipo, (tipo === 'Bodega' ? bodegas : productos).find(d => d.id === id));
    }
    if (boton.dataset.eliminar) {
        if (!confirm('¿Eliminar este registro? Solo es posible si no tiene historial.')) return;
        const ruta = boton.dataset.eliminar === 'Bodega' ? 'bodegas' : 'productos';
        await api('/' + ruta + '/' + id, 'DELETE'); await ver(ruta); avisar('Registro eliminado.');
    }
    if (boton.dataset.existencias) {
        const idBodega = Number(boton.dataset.existencias);
        const lista = await api('/bodegas/' + idBodega + '/existencias');
        $('detalleBodega').hidden = false;
        $('detalleBodega').innerHTML = '<h3>Stock · ' + nombreBodega(idBodega) + '</h3>' + tabla(['Producto','Cantidad'], lista.map(d => [texto(d.nombre), d.cantidad]));
    }
}));
function opciones(lista) { return '<option value="">Selecciona una opción</option>' + lista.map(d => '<option value="' + d.id + '">' + texto(d.nombre) + '</option>').join(''); }
function agregarDetalle() {
    const fila = document.createElement('div'); fila.className = 'detalle';
    fila.innerHTML = '<label>Producto<select class="producto" required>' + opciones(productos) + '</select></label><label>Cantidad<input class="cantidadProducto" type="number" min="1" max="1000000" required></label><button type="button" class="secundario">Quitar</button>';
    fila.querySelector('button').onclick = () => fila.remove();
    $('detalles').appendChild(fila);
}
function camposMovimiento() {
    const form = $('formMovimiento'); const tipo = form.elements.tipo.value;
    $('campoOrigen').hidden = tipo === 'ENTRADA'; $('campoDestino').hidden = tipo === 'SALIDA';
    form.elements.bodegaOrigenId.required = tipo !== 'ENTRADA'; form.elements.bodegaDestinoId.required = tipo !== 'SALIDA';
}
$('formMovimiento').elements.tipo.onchange = camposMovimiento;
$('agregarProducto').onclick = agregarDetalle;
$('nuevoMovimiento').onclick = tarea(async () => {
    await catalogos(); const form = $('formMovimiento'); form.reset(); form.hidden = false;
    form.elements.bodegaOrigenId.innerHTML = opciones(bodegas); form.elements.bodegaDestinoId.innerHTML = opciones(bodegas);
    $('detalles').innerHTML = ''; agregarDetalle(); camposMovimiento();
});
$('formMovimiento').onsubmit = tarea(async e => {
    e.preventDefault(); const form = e.target; const tipo = form.elements.tipo.value;
    const detalles = [...document.querySelectorAll('#detalles .detalle')].map(f => ({ productoId: Number(f.querySelector('.producto').value), cantidad: Number(f.querySelector('.cantidadProducto').value) }));
    if (!detalles.length) throw new Error('Agrega al menos un producto.');
    const boton = form.querySelector('button:not([type="button"])'); boton.disabled = true;
    try {
        await api('/movimientos', 'POST', { tipo, bodegaOrigenId: tipo === 'ENTRADA' ? null : Number(form.elements.bodegaOrigenId.value), bodegaDestinoId: tipo === 'SALIDA' ? null : Number(form.elements.bodegaDestinoId.value), productos: detalles });
        form.hidden = true; await ver('movimientos'); avisar('Movimiento registrado. Tu inventario está al día ♡');
    } finally { boton.disabled = false; }
});
async function cargarMovimientos() {
    const params = new URLSearchParams(); const form = $('filtroMovimientos');
    for (const campo of ['desde','hasta']) if (form.elements[campo].value) params.set(campo, form.elements[campo].value);
    const datos = await api('/movimientos?' + params);
    $('listaMovimientos').innerHTML = tabla(['ID / Fecha UTC','Tipo','Usuario','Origen → Destino','Productos'], datos.map(m => [m.id + '<br>' + texto(m.fecha.replace('T',' ')), '<span class="badge">' + texto(m.tipo) + '</span>', texto(m.usuario), nombreBodega(m.bodegaOrigenId) + ' → ' + nombreBodega(m.bodegaDestinoId), m.productos.map(p => nombreProducto(p.productoId) + ' × ' + p.cantidad).join('<br>')]));
}
$('filtroMovimientos').onsubmit = tarea(async e => { e.preventDefault(); await cargarMovimientos(); });
$('limpiarFechas').onclick = tarea(async () => { $('filtroMovimientos').reset(); await cargarMovimientos(); });
async function cargarAuditorias() {
    const params = new URLSearchParams();
    for (const [k,v] of new FormData($('filtroAuditorias'))) if (v.trim()) params.set(k,v.trim());
    const datos = await api('/auditorias?' + params);
    $('listaAuditorias').innerHTML = tabla(['Fecha UTC','Usuario','Operación','Entidad','Cambios'], datos.map(a => [texto(a.fecha.replace('T',' ')), texto(a.usuario), '<span class="badge">' + texto(a.operacion) + '</span>', texto(a.entidad), '<details><summary>Ver valores</summary><strong>Antes</strong><pre>' + texto(a.valoresAnteriores) + '</pre><strong>Después</strong><pre>' + texto(a.valoresNuevos) + '</pre></details>']));
}
$('filtroAuditorias').onsubmit = tarea(async e => { e.preventDefault(); await cargarAuditorias(); });
