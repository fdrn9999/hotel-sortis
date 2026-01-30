import * as THREE from 'three'
import * as CANNON from 'cannon-es'

export interface DiceResult {
  die1: number
  die2: number
  die3: number
}

export class DiceScene {
  private scene: THREE.Scene
  private camera: THREE.PerspectiveCamera
  private renderer: THREE.WebGLRenderer
  private world: CANNON.World

  private dice: THREE.Mesh[] = []
  private diceBodies: CANNON.Body[] = []

  private animationId: number | null = null
  private container: HTMLElement

  private isRolling = false
  private onRollComplete: ((result: DiceResult) => void) | null = null

  constructor(container: HTMLElement) {
    this.container = container

    // Three.js 씬 초기화
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x1b1b27) // CLAUDE.md에서 짙은 남색

    // 카메라 설정
    this.camera = new THREE.PerspectiveCamera(
      45,
      container.clientWidth / container.clientHeight,
      0.1,
      1000
    )
    this.camera.position.set(0, 5, 10)
    this.camera.lookAt(0, 0, 0)

    // 렌더러 설정
    this.renderer = new THREE.WebGLRenderer({ antialias: true })
    this.renderer.setSize(container.clientWidth, container.clientHeight)
    this.renderer.setPixelRatio(window.devicePixelRatio)
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
    container.appendChild(this.renderer.domElement)

    // Cannon.js 물리 세계 초기화
    this.world = new CANNON.World()
    this.world.gravity.set(0, -9.82, 0) // 중력
    this.world.broadphase = new CANNON.NaiveBroadphase()
    this.world.solver.iterations = 10

    this.setupLights()
    this.setupTable()
    this.createDice()

    // 리사이즈 핸들러
    window.addEventListener('resize', this.handleResize.bind(this))

    // 애니메이션 시작
    this.animate()
  }

  private setupLights(): void {
    // 앰비언트 라이트
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
    this.scene.add(ambientLight)

    // 디렉셔널 라이트 (샹들리에 조명 느낌)
    const directionalLight = new THREE.DirectionalLight(0xfffdd0, 1) // 크림색
    directionalLight.position.set(5, 10, 5)
    directionalLight.castShadow = true
    directionalLight.shadow.camera.left = -10
    directionalLight.shadow.camera.right = 10
    directionalLight.shadow.camera.top = 10
    directionalLight.shadow.camera.bottom = -10
    directionalLight.shadow.mapSize.width = 2048
    directionalLight.shadow.mapSize.height = 2048
    this.scene.add(directionalLight)

    // 포인트 라이트 (금빛 반사)
    const pointLight = new THREE.PointLight(0xd4af37, 0.5, 20) // 금색
    pointLight.position.set(0, 5, 0)
    this.scene.add(pointLight)
  }

  private setupTable(): void {
    // 테이블 (나무 탁자)
    const tableGeometry = new THREE.BoxGeometry(8, 0.5, 6)
    const tableMaterial = new THREE.MeshStandardMaterial({
      color: 0x8b4513, // 갈색 나무
      roughness: 0.8,
      metalness: 0.2
    })
    const table = new THREE.Mesh(tableGeometry, tableMaterial)
    table.position.set(0, -0.25, 0)
    table.receiveShadow = true
    this.scene.add(table)

    // 물리 바닥
    const groundShape = new CANNON.Box(new CANNON.Vec3(4, 0.25, 3))
    const groundBody = new CANNON.Body({
      mass: 0, // 정적 객체
      shape: groundShape,
      material: new CANNON.Material({ friction: 0.5, restitution: 0.3 })
    })
    groundBody.position.set(0, -0.25, 0)
    this.world.addBody(groundBody)
  }

  private createDice(): void {
    // 3개의 주사위 생성
    for (let i = 0; i < 3; i++) {
      // Three.js 주사위 (시각적)
      const diceGeometry = new THREE.BoxGeometry(0.16, 0.16, 0.16) // 16mm
      const diceMaterial = new THREE.MeshStandardMaterial({
        color: 0xfffdd0, // 상아색
        roughness: 0.5,
        metalness: 0.1
      })
      const dice = new THREE.Mesh(diceGeometry, diceMaterial)
      dice.castShadow = true
      dice.receiveShadow = true

      // 초기 위치 (테이블 위)
      dice.position.set(i * 0.5 - 0.5, 2, 0)
      this.scene.add(dice)
      this.dice.push(dice)

      // Cannon.js 주사위 (물리)
      const diceShape = new CANNON.Box(new CANNON.Vec3(0.08, 0.08, 0.08))
      const diceBody = new CANNON.Body({
        mass: 0.1, // 0.1kg
        shape: diceShape,
        material: new CANNON.Material({ friction: 0.5, restitution: 0.3 })
      })
      diceBody.position.set(i * 0.5 - 0.5, 2, 0)
      this.world.addBody(diceBody)
      this.diceBodies.push(diceBody)
    }
  }

  private animate(): void {
    this.animationId = requestAnimationFrame(() => this.animate())

    // 물리 시뮬레이션 업데이트
    this.world.step(1 / 60)

    // Three.js 오브젝트를 Cannon.js 위치/회전에 동기화
    for (let i = 0; i < 3; i++) {
      this.dice[i].position.copy(this.diceBodies[i].position as any)
      this.dice[i].quaternion.copy(this.diceBodies[i].quaternion as any)
    }

    // 주사위가 멈췄는지 확인
    if (this.isRolling) {
      const allStopped = this.diceBodies.every(body =>
        body.velocity.length() < 0.1 &&
        body.angularVelocity.length() < 0.1
      )

      if (allStopped) {
        this.isRolling = false
        const result = this.getDiceResult()
        if (this.onRollComplete) {
          this.onRollComplete(result)
        }
      }
    }

    // 렌더링
    this.renderer.render(this.scene, this.camera)
  }

  public roll(onComplete: (result: DiceResult) => void): void {
    if (this.isRolling) return

    this.isRolling = true
    this.onRollComplete = onComplete

    // 주사위를 테이블 위로 리셋
    for (let i = 0; i < 3; i++) {
      const body = this.diceBodies[i]

      // 위치 리셋
      body.position.set(
        Math.random() * 2 - 1, // -1 ~ 1
        3 + Math.random(), // 3 ~ 4
        Math.random() * 2 - 1
      )

      // 랜덤 회전
      body.quaternion.setFromEuler(
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2
      )

      // 랜덤 속도
      body.velocity.set(
        (Math.random() - 0.5) * 5,
        -2,
        (Math.random() - 0.5) * 5
      )

      // 랜덤 회전 속도
      body.angularVelocity.set(
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10
      )
    }
  }

  private getDiceResult(): DiceResult {
    // 주사위의 위쪽 면을 판정
    const results: number[] = []

    for (const body of this.diceBodies) {
      // 월드 좌표계에서 주사위의 위쪽 방향 벡터
      const up = new CANNON.Vec3(0, 1, 0)

      // 주사위의 6개 면 벡터
      const faces = [
        new CANNON.Vec3(0, 1, 0),  // 위 (면 1)
        new CANNON.Vec3(0, -1, 0), // 아래 (면 6)
        new CANNON.Vec3(1, 0, 0),  // 오른쪽 (면 2)
        new CANNON.Vec3(-1, 0, 0), // 왼쪽 (면 5)
        new CANNON.Vec3(0, 0, 1),  // 앞 (면 3)
        new CANNON.Vec3(0, 0, -1)  // 뒤 (면 4)
      ]

      // 각 면의 번호
      const faceValues = [1, 6, 2, 5, 3, 4]

      // 가장 위를 향하는 면 찾기
      let maxDot = -Infinity
      let faceIndex = 0

      for (let i = 0; i < faces.length; i++) {
        const faceWorld = body.quaternion.vmult(faces[i])
        const dot = faceWorld.dot(up)

        if (dot > maxDot) {
          maxDot = dot
          faceIndex = i
        }
      }

      results.push(faceValues[faceIndex])
    }

    return {
      die1: results[0],
      die2: results[1],
      die3: results[2]
    }
  }

  private handleResize(): void {
    const width = this.container.clientWidth
    const height = this.container.clientHeight

    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()

    this.renderer.setSize(width, height)
  }

  public dispose(): void {
    // 애니메이션 중지
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId)
    }

    // 리사이즈 핸들러 제거
    window.removeEventListener('resize', this.handleResize.bind(this))

    // Three.js 객체 정리
    this.dice.forEach(dice => {
      dice.geometry.dispose()
      if (Array.isArray(dice.material)) {
        dice.material.forEach(mat => mat.dispose())
      } else {
        dice.material.dispose()
      }
    })

    this.scene.clear()
    this.renderer.dispose()

    // DOM에서 canvas 제거
    if (this.renderer.domElement.parentElement) {
      this.renderer.domElement.parentElement.removeChild(this.renderer.domElement)
    }
  }
}
